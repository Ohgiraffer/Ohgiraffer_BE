package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface SpringDataLeaveBalanceRepository extends JpaRepository<LeaveBalanceJpaEntity, Long> {

    Optional<LeaveBalanceJpaEntity> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE LeaveBalanceJpaEntity l
        SET l.usedDays = l.usedDays + :amount
        WHERE l.userId = :userId
          AND (l.totalDays - l.usedDays) >= :amount
        """)
    int tryConsume(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE LeaveBalanceJpaEntity l
        SET l.totalDays = :targetTotalDays
        WHERE l.userId = :userId
          AND l.totalDays < :targetTotalDays
        """)
    int accrueTo(@Param("userId") Long userId, @Param("targetTotalDays") BigDecimal targetTotalDays);
}