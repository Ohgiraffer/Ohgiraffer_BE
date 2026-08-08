package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceScheduler {

    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final UserQueryUsecase userQueryUsecase;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void rolloverBalances() {
        LocalDate today = LocalDate.now();
        List<AttendancePeriodStartResult> startingPeriods = bootcampQueryUsecase.getPeriodsStartingOn(today);

        if (startingPeriods.isEmpty()) {
            log.info("[rolloverBalances] 오늘 시작하는 단위기간 없음 | date={}", today);
            return;
        }

        for (AttendancePeriodStartResult period : startingPeriods) {
            List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(period.bootcampId());

            for (Long studentId : studentIds) {
                createLeaveBalanceIfAbsent(studentId, period);
                createSickBalanceIfAbsent(studentId, period);
            }

            log.info("[rolloverBalances] 잔여 휴가/병결 이월 처리 완료 | bootcampId={}, periodNo={}, studentCount={}",
                    period.bootcampId(), period.periodNo(), studentIds.size());
        }
    }

    private void createLeaveBalanceIfAbsent(Long userId, AttendancePeriodStartResult period) {
        if (leaveBalanceRepository.existsByUserIdAndPeriodStart(userId, period.periodStart())) {
            return;
        }

        BigDecimal carriedOver = resolveCarriedOverLeaveDays(userId, period);

        leaveBalanceRepository.save(
                LeaveBalance.create(userId, period.periodStart(), period.periodEnd(), BigDecimal.ONE, carriedOver)
        );
    }

    private void createSickBalanceIfAbsent(Long userId, AttendancePeriodStartResult period) {
        if (sickBalanceRepository.existsByUserIdAndPeriodStart(userId, period.periodStart())) {
            return;
        }

        BigDecimal carriedOver = resolveCarriedOverSickDays(userId, period);

        sickBalanceRepository.save(
                SickBalance.create(userId, period.periodStart(), period.periodEnd(), BigDecimal.ONE, carriedOver)
        );
    }

    private BigDecimal resolveCarriedOverLeaveDays(Long userId, AttendancePeriodStartResult period) {
        if (period.periodNo() == 1) {
            return BigDecimal.ZERO;
        }
        LocalDate previousPeriodEnd = period.periodStart().minusDays(1);
        return leaveBalanceRepository.findByUserIdAndPeriodEnd(userId, previousPeriodEnd)
                .map(LeaveBalance::remainingDays)
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal resolveCarriedOverSickDays(Long userId, AttendancePeriodStartResult period) {
        if (period.periodNo() == 1) {
            return BigDecimal.ZERO;
        }
        LocalDate previousPeriodEnd = period.periodStart().minusDays(1);
        return sickBalanceRepository.findByUserIdAndPeriodEnd(userId, previousPeriodEnd)
                .map(SickBalance::remainingDays)
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }
}