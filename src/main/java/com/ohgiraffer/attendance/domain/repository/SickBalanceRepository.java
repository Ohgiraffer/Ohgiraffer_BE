package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.SickBalance;

import java.math.BigDecimal;
import java.util.Optional;

public interface SickBalanceRepository {
    Optional<SickBalance> findByUserId(Long userId);
    SickBalance save(SickBalance sickBalance);

    boolean tryConsume(Long userId, BigDecimal amount);
}