package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.SyncTriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceSheetAutoSyncScheduler {

    private final AttendanceSheetCommandUsecase attendanceSheetCommandUsecase;

    // 매일 09:20 ~ 09:40 1분 간격
    @Scheduled(cron = "0 20-40 9 * * *")
    public void syncMorning() {
        try {
            SyncAttendanceSheetResult result = attendanceSheetCommandUsecase.sync(
                    new SyncAttendanceSheetCommand(null, SyncTriggerType.SCHEDULED)
            );
            log.info("[syncMorning] 자동 동기화 완료 | total={}, success={}, failed={}",
                    result.totalCount(), result.successCount(), result.failedCount());
        } catch (Exception e) {
            log.error("[syncMorning] 자동 동기화 실패", e);
        }
    }
}