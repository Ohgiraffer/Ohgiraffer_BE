package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceCacheEvictor;
import com.ohgiraffer.attendance.application.command.RecordAttendanceSheetSyncLogCommand;
import com.ohgiraffer.attendance.application.command.SaveAttendanceExternalSheetLinkCommand;
import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetRowParser;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetRowSyncer;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetSyncLogRecorder;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetSyncResultResolver;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.*;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceSheetCommandService implements AttendanceSheetCommandUsecase {

    private static final int RETENTION_DAYS = 5;
    private static final String REASON_NAME_NOT_FOUND = "훈련생 식별자를 찾을 수 없음";
    private static final String REASON_UNKNOWN = "처리 중 오류가 발생했습니다";

    private final AttendanceExternalSheetLinkRepository attendanceExternalSheetLinkRepository;
    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;
    private final GoogleSheetsClient googleSheetsClient;
    private final AttendanceSheetRowParser attendanceSheetRowParser;
    private final AttendanceSheetRowSyncer attendanceSheetRowSyncer;
    private final AttendanceSheetSyncLogRecorder attendanceSheetSyncLogRecorder;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceCacheEvictor attendanceCacheEvictor;
    private final GetUserNamesPort getUserNamesPort;

    @Override
    @Transactional
    public void save(SaveAttendanceExternalSheetLinkCommand command) {
        AttendanceExternalSheetLink existing = attendanceExternalSheetLinkRepository.findLatest()
                .orElse(null);

        AttendanceExternalSheetLink toSave = AttendanceExternalSheetLink.builder()
                .attendanceSheetLinkId(existing != null ? existing.getAttendanceSheetLinkId() : null)
                .sheetUrl(command.sheetUrl())
                .tabName(command.tabName())
                .dateCellRange(command.dateCellRange())
                .columnMapping(command.columnMapping())
                .lastSyncedAt(existing != null ? existing.getLastSyncedAt() : null)
                .build();

        attendanceExternalSheetLinkRepository.save(toSave);
    }

    @Override
    @Transactional
    public SyncAttendanceSheetResult sync(SyncAttendanceSheetCommand command) {
        AttendanceExternalSheetLink link = attendanceExternalSheetLinkRepository.findLatest()
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SHEET_LINK_NOT_FOUND));

        String spreadsheetId = attendanceSheetRowParser.extractSpreadsheetId(link.getSheetUrl());
        LocalDate targetDate = attendanceSheetRowParser.readSelectedDate(spreadsheetId, link.getDateCellRange());

        if (command.trigger() == SyncTriggerType.SCHEDULED && !targetDate.equals(LocalDate.now())) {
            String reason = "시트에 선택된 날짜(" + targetDate + ")가 오늘과 달라 자동 동기화를 건너뜀";
            log.warn("[sync] {}", reason);
            attendanceSheetSyncLogRecorder.record(new RecordAttendanceSheetSyncLogCommand(
                    link.getAttendanceSheetLinkId(), null, reason, List.of(), null, "스케줄러", SyncResult.FAIL
            ));
            return SyncAttendanceSheetResult.of(0, 0, List.of());
        }

        List<List<Object>> rows = googleSheetsClient.readRange(spreadsheetId, link.getTabName());
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시트에 데이터가 없습니다.");
        }

        Map<String, Integer> columnIndex = attendanceSheetRowParser.buildColumnIndex(rows.get(0), link.getColumnMapping());

        boolean provisional = command.trigger() == SyncTriggerType.SCHEDULED;

        // 부트캠프 ID 하나만 조회 (시트는 단일 부트캠프 기준)
        Long bootcampId = command.trigger() == SyncTriggerType.SCHEDULED
                ? userQueryUsecase.getAnyActiveBootcampId()
                : userQueryUsecase.getBootcampId(command.userId());

        Map<Long, UserStatus> statusMap = userQueryUsecase.getStudentStatusesByBootcampId(bootcampId).stream()
                .collect(Collectors.toMap(StudentStatusView::userId, StudentStatusView::status));
        Map<String, Long> userIdByName = userQueryUsecase.getStudentNameToIdMapByBootcampId(bootcampId);

        int totalCount = 0;
        int successCount = 0;
        List<FailedRowDetail> failedRows = new ArrayList<>();
        Set<Long> touchedUserIds = new HashSet<>();

        for (int rowNum = 1; rowNum < rows.size(); rowNum++) {
            List<Object> row = rows.get(rowNum);
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            String name = attendanceSheetRowParser.cell(row, columnIndex.get("name"));

            try {
                Long userId = userIdByName.get(name);
                if (userId == null) {
                    failedRows.add(new FailedRowDetail(rowNum, REASON_NAME_NOT_FOUND));
                    continue;
                }

                UserStatus currentStatus = statusMap.get(userId);
                if (currentStatus == UserStatus.WITHDRAWN || currentStatus == UserStatus.EXPELLED) {
                    continue; // 자퇴/제적 학생은 조용히 건너뜀
                }

                AttendanceSyncOutcome outcome = attendanceSheetRowSyncer.syncOneRow(userId, targetDate, row, columnIndex, provisional);
                switch (outcome) {
                    case SKIPPED -> totalCount--;
                    case UNCHANGED -> { /* 처리는 했지만 변동 없음 - totalCount만 유지 */ }
                    case CHANGED -> {
                        touchedUserIds.add(userId);
                        successCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("[sync] 행 처리 실패 | row={}, name={}", rowNum, name, e);
                String reason = e.getMessage() != null ? e.getMessage() : REASON_UNKNOWN;
                failedRows.add(new FailedRowDetail(rowNum, reason));
            }
        }

        for (Long userId : touchedUserIds) {
            attendanceCacheEvictor.evictSummary(userId);
        }
        if (!touchedUserIds.isEmpty()) {
            attendanceCacheEvictor.evictAllForBootcamp(bootcampId);
        }

        SyncResult result = AttendanceSheetSyncResultResolver.resolve(totalCount, failedRows.size());
        String diffSummary = String.valueOf(successCount);

        String executorName = command.userId() != null
                ? getUserNamesPort.findNamesByUserIds(List.of(command.userId())).getOrDefault(command.userId(), "알 수 없음")
                : "스케줄러";

        attendanceSheetSyncLogRecorder.record(new RecordAttendanceSheetSyncLogCommand(
                link.getAttendanceSheetLinkId(),
                link.getTabName() + "!A1:Z" + rows.size(),
                diffSummary,
                failedRows,
                command.userId(),
                executorName,
                result
        ));

        return SyncAttendanceSheetResult.of(totalCount, successCount, failedRows);
    }

    @Override
    @Transactional
    public void cleanupLogs() {
        LocalDateTime cutoff = LocalDate.now().minusDays(RETENTION_DAYS).atStartOfDay();
        attendanceSheetSyncLogRepository.deleteBefore(cutoff);
        log.info("[cleanupLogs] {}일 이전 시트 동기화 이력 삭제 완료 | cutoff={}", RETENTION_DAYS, cutoff);
    }
}