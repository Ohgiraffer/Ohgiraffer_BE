package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        List<AttendancePeriodStartResult> startingPeriods = bootcampQueryUsecase.getPeriodsStartingOn(today);

        if (startingPeriods.isEmpty()) {
            log.info("[rolloverBalances] 오늘 시작하는 단위기간 없음 | date={}", today);
            return;
        }

        int totalFailCount = 0;

        for (AttendancePeriodStartResult period : startingPeriods) {
            List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(period.bootcampId());
            int successCount = 0;
            int failCount = 0;

            for (Long studentId : studentIds) {
                try {
                    processor.processStudent(studentId, period);
                    successCount++;
                } catch (DataIntegrityViolationException e) {
                    if (isDuplicateKeyException(e)) {
                        log.warn("[rolloverBalances] 중복 키 — 이미 존재하는 잔액으로 처리 | studentId={}, periodNo={}",
                                studentId, period.periodNo());
                        successCount++;
                    } else {
                        log.error("[rolloverBalances] 무결성 위반 처리 실패 | studentId={}, periodNo={}, error={}",
                                studentId, period.periodNo(), e.getMessage());
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[rolloverBalances] 처리 실패 | studentId={}, periodNo={}, error={}",
                            studentId, period.periodNo(), e.getMessage());
                    failCount++;
                }
            }

            if (failCount > 0) {
                log.warn("[rolloverBalances] 이월 처리 완료 (일부 실패) | bootcampId={}, periodNo={}, success={}, fail={}",
                        period.bootcampId(), period.periodNo(), successCount, failCount);
                totalFailCount += failCount;
            } else {
                log.info("[rolloverBalances] 이월 처리 완료 | bootcampId={}, periodNo={}, success={}",
                        period.bootcampId(), period.periodNo(), successCount);
            }
        }

        if (totalFailCount > 0) {
            throw new RuntimeException(
                    String.format("[rolloverBalances] 일부 학생 처리 실패 | totalFail=%d", totalFailCount)
            );
        }
    }

    private boolean isDuplicateKeyException(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && msg.contains("Duplicate entry") &&
                    (msg.contains("UQ_leave_balance_user_period") || msg.contains("UQ_sick_balance_user_period"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}