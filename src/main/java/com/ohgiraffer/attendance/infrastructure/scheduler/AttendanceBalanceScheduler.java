package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceScheduler {

    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceBalanceProcessor processor;

    @Scheduled(cron = "0 5 0 * * *")
    public void rolloverBalances() {
        LocalDate today = LocalDate.now();

        List<Long> bootcampIds = bootcampQueryUsecase.getActivePeriods(today).stream()
                .map(AttendancePeriodStartResult::bootcampId)
                .distinct()
                .toList();

        if (bootcampIds.isEmpty()) {
            log.info("[rolloverBalances] 진행 중인 부트캠프 없음 | date={}", today);
            return;
        }

        for (Long bootcampId : bootcampIds) {
            BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
            List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(bootcampId);

            int successCount = 0;
            int failCount = 0;

            for (Long studentId : studentIds) {
                try {
                    processor.ensureLeaveBalance(studentId, bootcampPeriod, today);
                    processor.ensureSickBalance(studentId, bootcampPeriod);
                    successCount++;
                } catch (Exception e) {
                    log.error("[rolloverBalances] 처리 실패 | studentId={}, bootcampId={}, error={}",
                            studentId, bootcampId, e.getMessage());
                    failCount++;
                }
            }

            if (failCount > 0) {
                log.warn("[rolloverBalances] 처리 완료 (일부 실패) | bootcampId={}, success={}, fail={}",
                        bootcampId, successCount, failCount);
            } else {
                log.info("[rolloverBalances] 처리 완료 | bootcampId={}, success={}", bootcampId, successCount);
            }
        }
    }
}