package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceProcessor {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processStudent(Long studentId, AttendancePeriodStartResult period) {
        createLeaveBalanceIfAbsent(studentId, period);
        createSickBalanceIfAbsent(studentId, period);
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