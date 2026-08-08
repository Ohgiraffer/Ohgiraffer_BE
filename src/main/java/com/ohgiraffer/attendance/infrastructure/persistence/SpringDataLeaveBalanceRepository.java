package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataLeaveBalanceRepository  extends JpaRepository<LeaveBalanceJpaEntity, Long> {

    // user_id가 일치하면서 오늘 날짜가 period_start~period_end 범위 안에 있는 행을 찾기
    Optional<LeaveBalanceJpaEntity> findByUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            Long userId, LocalDate periodStartBefore, LocalDate periodEndAfter
    );

    Optional<LeaveBalanceJpaEntity> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd);

    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);
}