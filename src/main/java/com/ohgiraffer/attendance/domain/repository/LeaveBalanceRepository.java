package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;

import java.time.LocalDate;
import java.util.Optional;

public interface LeaveBalanceRepository {
    Optional<LeaveBalance> findCurrentByUserId(Long userId, LocalDate referenceDate);
    Optional<LeaveBalance> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd);
    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);
    LeaveBalance save(LeaveBalance leaveBalance);
}