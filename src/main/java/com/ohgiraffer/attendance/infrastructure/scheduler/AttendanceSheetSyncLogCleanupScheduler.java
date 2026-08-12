package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceSheetSyncLogCleanupScheduler {

    private final AttendanceSheetCommandUsecase attendanceSheetCommandUsecase;

    // 매일 00:05에 5일 지난 동기화 이력 삭제
    @Scheduled(cron = "0 5 0 * * *")
    public void cleanup() {
        try {
            attendanceSheetCommandUsecase.cleanupLogs();
        } catch (Exception e) {
            log.error("[cleanup] 시트 동기화 이력 정리 실패", e);
        }
    }
}