package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceProcessor {

    private static final BigDecimal SICK_DAY_RATE = new BigDecimal("0.1");

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LeaveBalance ensureLeaveBalance(Long userId, BootcampPeriodResult bootcampPeriod, LocalDate today) {
        LeaveBalance current = leaveBalanceRepository.findByUserId(userId)
                .orElseGet(() -> saveNewLeaveBalance(userId));

        int elapsedMonths = elapsedAccrualMonths(bootcampPeriod, today);
        LeaveBalance updated = current.accrueUpTo(elapsedMonths);

        if (updated == current) {
            return current;
        }
        return leaveBalanceRepository.save(updated);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SickBalance ensureSickBalance(Long userId, BootcampPeriodResult bootcampPeriod) {
        return sickBalanceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    BigDecimal totalDays = resolveSickTotalDays(bootcampPeriod);
                    try {
                        return sickBalanceRepository.save(SickBalance.create(userId, totalDays));
                    } catch (DataIntegrityViolationException e) {
                        log.warn("[SickBalance] 동시 생성 충돌, 기존 행 재조회 | userId={}", userId);
                        return sickBalanceRepository.findByUserId(userId).orElseThrow(() -> e);
                    }
                });
    }

    private LeaveBalance saveNewLeaveBalance(Long userId) {
        try {
            return leaveBalanceRepository.save(LeaveBalance.createEmpty(userId));
        } catch (DataIntegrityViolationException e) {
            log.warn("[LeaveBalance] 동시 생성 충돌, 기존 행 재조회 | userId={}", userId);
            return leaveBalanceRepository.findByUserId(userId).orElseThrow(() -> e);
        }
    }

    private int elapsedAccrualMonths(BootcampPeriodResult bootcampPeriod, LocalDate today) {
        LocalDate cappedToday = today.isAfter(bootcampPeriod.endDate()) ? bootcampPeriod.endDate() : today;
        if (cappedToday.isBefore(bootcampPeriod.startDate())) {
            return 0;
        }
        return (int) Period.between(bootcampPeriod.startDate(), cappedToday).toTotalMonths();
    }

    private BigDecimal resolveSickTotalDays(BootcampPeriodResult bootcampPeriod) {
        long weekdays = AttendanceMetricsCalculator.countWeekdays(bootcampPeriod.startDate(), bootcampPeriod.endDate());
        return BigDecimal.valueOf(weekdays)
                .multiply(SICK_DAY_RATE)
                .setScale(0, RoundingMode.HALF_UP);
    }
}