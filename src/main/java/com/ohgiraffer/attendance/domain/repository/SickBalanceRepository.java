package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.SickBalance;

import java.time.LocalDate;
import java.util.Optional;

public interface SickBalanceRepository {
    Optional<SickBalance> findCurrentByUserId(Long userId, LocalDate referenceDate);
    Optional<SickBalance> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd);
    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);
    SickBalance save(SickBalance sickBalance);
}