package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceCacheEvictor;
import com.ohgiraffer.attendance.application.command.RecordAttendanceSheetSyncLogCommand;
import com.ohgiraffer.attendance.application.command.SaveAttendanceExternalSheetLinkCommand;
import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetRowParser;
import com.ohgiraffer.attendance.application.helper.AttendanceStatusResolver;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.domain.dto.AttendanceExternalSheetLinkView;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.*;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceSheetCommandService implements AttendanceSheetCommandUsecase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int RETENTION_DAYS = 5;

    private final AttendanceExternalSheetLinkRepository attendanceExternalSheetLinkRepository;
    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final GoogleSheetsClient googleSheetsClient;
    private final AttendanceStatusResolver attendanceStatusResolver;
    private final AttendanceSheetRowParser attendanceSheetRowParser;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceCacheEvictor attendanceCacheEvictor;
    private final GetUserNamesPort getUserNamesPort;

    @Override
    @Transactional
    public AttendanceExternalSheetLinkView save(SaveAttendanceExternalSheetLinkCommand command) {
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

        AttendanceExternalSheetLink saved = attendanceExternalSheetLinkRepository.save(toSave);
        return AttendanceExternalSheetLinkView.from(saved);
    }

    @Override
    @Transactional
    public SyncAttendanceSheetResult sync(SyncAttendanceSheetCommand command) {
        AttendanceExternalSheetLink link = attendanceExternalSheetLinkRepository.findLatest()
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SHEET_LINK_NOT_FOUND));

        String spreadsheetId = attendanceSheetRowParser.extractSpreadsheetId(link.getSheetUrl());

        LocalDate targetDate = readSelectedDate(spreadsheetId, link.getDateCellRange());

        if (command.trigger() == SyncTriggerType.SCHEDULED && !targetDate.equals(LocalDate.now())) {
            String reason = "시트에 선택된 날짜(" + targetDate + ")가 오늘과 달라 자동 동기화를 건너뜀";
            log.warn("[sync] {}", reason);
            recordLog(new RecordAttendanceSheetSyncLogCommand(
                    link.getAttendanceSheetLinkId(), null, reason, null, "스케줄러", SyncResult.FAIL
            ));
            return new SyncAttendanceSheetResult(0, 0, 0, List.of());
        }

        List<List<Object>> rows = googleSheetsClient.readRange(spreadsheetId, link.getTabName());

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시트에 데이터가 없습니다.");
        }

        Map<String, Integer> columnIndex = attendanceSheetRowParser.buildColumnIndex(rows.get(0), link.getColumnMapping());

        boolean provisional = command.trigger() == SyncTriggerType.SCHEDULED;

        int totalCount = 0;
        int successCount = 0;
        List<SyncAttendanceSheetResult.FailedRow> failedRows = new ArrayList<>();
        Set<Long> touchedBootcampIds = new HashSet<>();
        Set<Long> touchedUserIds = new HashSet<>();

        Map<Long, Map<Long, UserStatus>> statusByBootcamp = new HashMap<>();

        for (int rowNum = 1; rowNum < rows.size(); rowNum++) {
            List<Object> row = rows.get(rowNum);
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            String name = cell(row, columnIndex.get("name"));

            try {
                Long userId = userQueryUsecase.findActiveUserIdByName(name)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "일치하는 재원생을 찾을 수 없습니다: " + name));

                Long bootcampId = userQueryUsecase.getBootcampId(userId);

                Map<Long, UserStatus> statusMap = statusByBootcamp.computeIfAbsent(
                        bootcampId,
                        id -> userQueryUsecase.getStudentStatusesByBootcampId(id).stream()
                                .collect(Collectors.toMap(StudentStatusView::userId, StudentStatusView::status))
                );

                UserStatus currentStatus = statusMap.get(userId);
                if (currentStatus == UserStatus.WITHDRAWN || currentStatus == UserStatus.EXPELLED) {
                    continue; // 자퇴/제적 학생은 조용히 건너뜀
                }

                syncOneRow(userId, today, row, columnIndex);

                touchedUserIds.add(userId);
                touchedBootcampIds.add(bootcampId);
                successCount++;
            } catch (Exception e) {
                log.warn("[sync] 행 처리 실패 | row={}, name={}", rowNum, name, e);
                failedRows.add(new SyncAttendanceSheetResult.FailedRow(rowNum, name, e.getMessage()));
            }
        }

        for (Long userId : touchedUserIds) {
            attendanceCacheEvictor.evictSummary(userId);
        }
        for (Long bootcampId : touchedBootcampIds) {
            attendanceCacheEvictor.evictAllForBootcamp(bootcampId);
        }

        SyncResult result = failedRows.isEmpty() ? SyncResult.SUCCESS : SyncResult.FAIL;
        String diffSummary = String.format("[%s] 총 %d건 중 성공 %d건, 실패 %d건",
                targetDate, totalCount, successCount, failedRows.size());

        String executorName = command.userId() != null
                ? getUserNamesPort.findNamesByUserIds(List.of(command.userId())).getOrDefault(command.userId(), "알 수 없음")
                : "스케줄러";

        recordLog(new RecordAttendanceSheetSyncLogCommand(
                link.getAttendanceSheetLinkId(),
                link.getTabName() + "!A1:Z" + rows.size(),
                diffSummary,
                command.userId(),
                executorName,
                result
        ));

        return new SyncAttendanceSheetResult(totalCount, successCount, failedRows.size(), failedRows);
    }

    @Override
    @Transactional
    public void cleanupLogs() {
        LocalDateTime cutoff = LocalDate.now().minusDays(RETENTION_DAYS).atStartOfDay();
        attendanceSheetSyncLogRepository.deleteBefore(cutoff);
        log.info("[cleanupLogs] {}일 이전 시트 동기화 이력 삭제 완료 | cutoff={}", RETENTION_DAYS, cutoff);
    }

    private void recordLog(RecordAttendanceSheetSyncLogCommand command) {
        AttendanceSheetSyncLog logEntry = AttendanceSheetSyncLog.builder()
                .attendanceSheetLinkId(command.attendanceSheetLinkId())
                .changedRange(command.changedRange())
                .diffSummary(command.diffSummary())
                .executorId(command.executorId())
                .executorName(command.executorName())
                .result(command.result())
                .build();

        attendanceSheetSyncLogRepository.save(logEntry);

        if (command.result() == SyncResult.SUCCESS && command.attendanceSheetLinkId() != null) {
            attendanceExternalSheetLinkRepository.findById(command.attendanceSheetLinkId())
                    .ifPresent(link -> attendanceExternalSheetLinkRepository.save(link.withSyncedNow(LocalDateTime.now())));
        }
    }

    // 반영했으면 true, 아직 입실 기록이 없어 스킵했으면 false
    private boolean syncOneRow(Long userId, LocalDate targetDate, List<Object> row, Map<String, Integer> columnIndex, boolean provisional) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDateForUpdate(userId, targetDate);

        if (existing.isPresent()
                && (existing.get().getStatus() == AttendanceStatus.LEAVE || existing.get().getStatus() == AttendanceStatus.SICK)) {
            return false; // 승인된 휴가/병결은 시트가 건드리지 않음
        }

        LocalTime checkInTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkIn")));
        LocalTime checkOutTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkOut")));
        LocalTime outingTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("outing")));
        LocalTime returnTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("return")));

        AttendanceStatus status;
        if (provisional) {
            status = attendanceStatusResolver.resolveProvisional(checkInTime);
            if (status == null) {
                return false; // 아직 입실 기록 없음 - 다음 회차에 재확인
            }
        } else {
            status = attendanceStatusResolver.resolve(checkInTime, checkOutTime, outingTime, returnTime);
        }

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(existing.get().getId(), userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null)
                : Attendance.create(userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null);

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            Attendance reloaded = attendanceRepository.findByUserIdAndDate(userId, targetDate)
                    .orElseThrow(() -> e);
            Attendance retry = Attendance.reconstitute(reloaded.getId(), userId, targetDate, status,
                    checkInTime, checkOutTime, outingTime, returnTime, null);
            attendanceRepository.save(retry);
        }

        return true;
    }

    private LocalDate readSelectedDate(String spreadsheetId, String dateCellRange) {
        List<List<Object>> cell = googleSheetsClient.readRange(spreadsheetId, dateCellRange);
        if (cell.isEmpty() || cell.get(0).isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "날짜 선택 셀에서 값을 읽을 수 없습니다.");
        }

        String raw = String.valueOf(cell.get(0).get(0)).trim();
        try {
            return LocalDate.parse(raw, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "날짜 셀 형식이 올바르지 않습니다 (yyyy-MM-dd): " + raw);
        }
    }
}