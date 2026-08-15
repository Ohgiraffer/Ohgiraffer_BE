package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceProcessor {

    private static final BigDecimal SICK_DAY_RATE = new BigDecimal("0.1");

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processStudent(Long studentId, AttendancePeriodStartResult period) {
        createLeaveBalanceIfAbsent(studentId, period);
        createSickBalanceIfAbsent(studentId, period);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LeaveBalance createLeaveBalanceIfAbsent(Long userId, AttendancePeriodStartResult period) {
        return leaveBalanceRepository.findByUserIdAndPeriodStart(userId, period.periodStart())
                .orElseGet(() -> {
                    BigDecimal carriedOver = resolveCarriedOverLeaveDays(userId, period);
                    return leaveBalanceRepository.save(
                            LeaveBalance.create(userId, period.periodStart(), period.periodEnd(), BigDecimal.ONE, carriedOver)
                    );
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SickBalance createSickBalanceIfAbsent(Long userId, AttendancePeriodStartResult period) {
        return sickBalanceRepository.findByUserIdAndPeriodStart(userId, period.periodStart())
                .orElseGet(() -> {
                    BigDecimal carriedOver = resolveCarriedOverSickDays(userId, period);
                    BigDecimal totalDays = resolveSickTotalDays(period);
                    return sickBalanceRepository.save(
                            SickBalance.create(userId, period.periodStart(), period.periodEnd(), totalDays, carriedOver)
                    );
                });
    }

    private BigDecimal resolveSickTotalDays(AttendancePeriodStartResult period) {
        long weekdays = AttendanceMetricsCalculator.countWeekdays(period.periodStart(), period.periodEnd());
        return BigDecimal.valueOf(weekdays)
                .multiply(SICK_DAY_RATE)
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveCarriedOverLeaveDays(Long userId, AttendancePeriodStartResult period) {
        if (period.periodNo() == 1) return BigDecimal.ZERO;
        LocalDate previousPeriodEnd = period.periodStart().minusDays(1);
        return leaveBalanceRepository.findByUserIdAndPeriodEnd(userId, previousPeriodEnd)
                .map(lb -> BigDecimal.valueOf(lb.remainingDays()))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal resolveCarriedOverSickDays(Long userId, AttendancePeriodStartResult period) {
        if (period.periodNo() == 1) return BigDecimal.ZERO;
        LocalDate previousPeriodEnd = period.periodStart().minusDays(1);
        return sickBalanceRepository.findByUserIdAndPeriodEnd(userId, previousPeriodEnd)
                .map(sb -> BigDecimal.valueOf(sb.remainingDays()))
                .orElse(BigDecimal.ZERO);
    }
}