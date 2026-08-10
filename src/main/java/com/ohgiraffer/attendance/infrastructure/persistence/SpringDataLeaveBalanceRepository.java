package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataLeaveBalanceRepository  extends JpaRepository<LeaveBalanceJpaEntity, Long> {

    // user_id가 일치하면서 오늘 날짜가 period_start~period_end 범위 안에 있는 행을 찾기
    Optional<LeaveBalanceJpaEntity> findByUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            Long userId, LocalDate periodStartBefore, LocalDate periodEndAfter
    );

    Optional<LeaveBalanceJpaEntity> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd);

    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE LeaveBalanceJpaEntity l
    SET l.usedDays = l.usedDays + :amount
    WHERE l.userId = :userId
      AND l.periodStart = :periodStart
      AND (l.totalDays + l.carriedOverDays - l.usedDays) >= :amount
    """)
    int tryConsume(@Param("userId") Long userId, @Param("periodStart") LocalDate periodStart, @Param("amount") BigDecimal amount);
}