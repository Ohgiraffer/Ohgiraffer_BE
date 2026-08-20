package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.SyncTriggerType;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceSheetAutoSyncScheduler {

    private final AttendanceSheetCommandUsecase attendanceSheetCommandUsecase;

    // 평일(월~금) 09:20 ~ 09:40 1분 간격
    @Scheduled(cron = "0 20-40 9 * * MON-FRI")
    public void syncMorning() {
        try {
            SyncAttendanceSheetResult result = attendanceSheetCommandUsecase.sync(
                    new SyncAttendanceSheetCommand(null, SyncTriggerType.SCHEDULED)
            );
            log.info("[syncMorning] 자동 동기화 완료 | total={}, success={}, failed={}",
                    result.totalCount(), result.successCount(), result.failedCount());
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.LOCK_ACQUISITION_FAILED) {
                log.warn("[syncMorning] 다른 동기화가 진행 중이라 이번 스케줄은 건너뜀");
                return;
            }
            log.error("[syncMorning] 자동 동기화 실패", e);
        } catch (Exception e) {
            log.error("[syncMorning] 자동 동기화 실패", e);
        }
    }
}