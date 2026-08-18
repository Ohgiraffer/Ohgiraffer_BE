package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceCacheEvictor;
import com.ohgiraffer.attendance.application.command.RecordAttendanceSheetSyncLogCommand;
import com.ohgiraffer.attendance.application.command.SaveAttendanceExternalSheetLinkCommand;
import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetRowParser;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetRowSyncer;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetSyncLogRecorder;
import com.ohgiraffer.attendance.application.helper.AttendanceSheetSyncResultResolver;
import com.ohgiraffer.attendance.application.port.GetBootcampStaffIdsPort;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.attendance.application.port.NotifiedTodayCheckPort;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.*;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceSheetCommandService implements AttendanceSheetCommandUsecase {

    private static final int RETENTION_DAYS = 5;
    private static final String REASON_EMAIL_NOT_FOUND = "이메일로 훈련생을 찾을 수 없음";
    private static final String REASON_UNKNOWN = "처리 중 오류가 발생했습니다";
    private static final String RELATED_ENTITY_TYPE = "ATTENDANCE";

    private final AttendanceExternalSheetLinkRepository attendanceExternalSheetLinkRepository;
    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;
    private final GoogleSheetsClient googleSheetsClient;
    private final AttendanceSheetRowParser attendanceSheetRowParser;
    private final AttendanceSheetRowSyncer attendanceSheetRowSyncer;
    private final AttendanceSheetSyncLogRecorder attendanceSheetSyncLogRecorder;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceCacheEvictor attendanceCacheEvictor;
    private final GetUserNamesPort getUserNamesPort;
    private final AttendanceRepository attendanceRepository;
    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final GetBootcampStaffIdsPort getBootcampStaffIdsPort;
    private final NotifiedTodayCheckPort notifiedTodayCheckPort;
    private final ApplicationEventPublisher eventPublisher;

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

            AttendanceSheetSyncLog skippedLog = AttendanceSheetSyncLog.builder()
                    .attendanceSheetLinkId(link.getAttendanceSheetLinkId())
                    .changedRange(null)
                    .diffSummary("0")
                    .failedRowDetails(List.of())
                    .executorId(null)
                    .executorName("스케줄러")
                    .result(SyncResult.FAIL)
                    .build();
            attendanceSheetSyncLogRepository.save(skippedLog);

            return SyncAttendanceSheetResult.of(0, 0, List.of());
        }

        List<List<Object>> rows = googleSheetsClient.readRange(spreadsheetId, link.getTabName());
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시트에 데이터가 없습니다.");
        }

        Map<String, Integer> columnIndex = attendanceSheetRowParser.buildColumnIndex(rows.get(0), link.getColumnMapping());

        boolean provisional = command.trigger() == SyncTriggerType.SCHEDULED;

        Long bootcampId = command.trigger() == SyncTriggerType.SCHEDULED
                ? userQueryUsecase.getAnyActiveBootcampId()
                : userQueryUsecase.getBootcampId(command.userId());

        Map<Long, UserStatus> statusMap = userQueryUsecase.getStudentStatusesByBootcampId(bootcampId).stream()
                .collect(Collectors.toMap(StudentStatusView::userId, StudentStatusView::status));
        Map<String, Long> userIdByEmail = userQueryUsecase.getStudentEmailToIdMapByBootcampId(bootcampId);

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
            String email = attendanceSheetRowParser.cell(row, columnIndex.get("email"));

            try {
                Long userId = userIdByEmail.get(email);
                if (userId == null) {
                    failedRows.add(new FailedRowDetail(rowNum, REASON_EMAIL_NOT_FOUND));
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
                log.warn("[sync] 행 처리 실패 | row={}, email={}", rowNum, email, e);
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

        if (!touchedUserIds.isEmpty()) {
            checkAndNotifyRisk(touchedUserIds, bootcampId);
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

    //  touchedUserIds 순회하며 위험도 계산 후 임계치 도달 시에만 알림
    private void checkAndNotifyRisk(Set<Long> touchedUserIds, Long bootcampId) {
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);
        LocalDate today = LocalDate.now();

        if (today.isBefore(bootcampPeriod.startDate())) {
            return; // 아직 출결 기간 시작 전이면 판정하지 않음
        }

        LocalDate start = bootcampPeriod.startDate();
        LocalDate end = today.isBefore(bootcampPeriod.endDate()) ? today : bootcampPeriod.endDate();

        for (Long userId : touchedUserIds) {
            AttendanceSummaryView summary = attendanceRepository.countByUserAndDateRange(userId, start, end);

            BigDecimal attendanceRate = AttendanceMetricsCalculator.calculateAttendanceRate(
                    start, end,
                    summary.absentDays(), summary.lateCount(), summary.earlyLeaveCount(), summary.outingCount()
            );

            AttendanceRiskLevel riskLevel = AttendanceMetricsCalculator.calculateRiskLevel(attendanceRate, policy);

            if (riskLevel != null) {
                notifyRisk(userId, bootcampId, riskLevel);
            }
        }
    }

    // 훈련생 본인 + 담당 강사·매니저에게 알림 (오늘 이미 보냈으면 스킵)
    private void notifyRisk(Long studentUserId, Long bootcampId, AttendanceRiskLevel riskLevel) {
        if (notifiedTodayCheckPort.isNotifiedToday(studentUserId, NotificationType.ATTENDANCE_RISK)) {
            return; // 중복 방지 - 오늘 이미 이 학생에게 위험 알림이 나감
        }

        String title = "출석 위험 기준에 도달했습니다";
        String content = "출석률이 " + riskLevel.name() + " 단계에 도달했습니다. 확인이 필요합니다.";

        // 훈련생 본인
        eventPublisher.publishEvent(new NotificationRequestedEvent(
                studentUserId, NotificationType.ATTENDANCE_RISK, title, content, RELATED_ENTITY_TYPE, studentUserId
        ));

        // 담당 강사·매니저 전체
        List<Long> staffIds = getBootcampStaffIdsPort.findStaffIdsByBootcampId(bootcampId);
        for (Long staffId : staffIds) {
            eventPublisher.publishEvent(new NotificationRequestedEvent(
                    staffId, NotificationType.ATTENDANCE_RISK, title,
                    "훈련생 출석률이 " + riskLevel.name() + " 단계에 도달했습니다.",
                    RELATED_ENTITY_TYPE, studentUserId
            ));
        }
    }

    @Override
    @Transactional
    public void cleanupLogs() {
        LocalDateTime cutoff = LocalDate.now().minusDays(RETENTION_DAYS).atStartOfDay();
        attendanceSheetSyncLogRepository.deleteBefore(cutoff);
        log.info("[cleanupLogs] {}일 이전 시트 동기화 이력 삭제 완료 | cutoff={}", RETENTION_DAYS, cutoff);
    }
}