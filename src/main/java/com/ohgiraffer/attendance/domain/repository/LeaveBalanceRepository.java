package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;

import java.math.BigDecimal;
import java.util.Optional;

public interface LeaveBalanceRepository {
    Optional<LeaveBalance> findByUserId(Long userId);
    LeaveBalance save(LeaveBalance leaveBalance);
    boolean tryConsume(Long userId, BigDecimal amount);
}