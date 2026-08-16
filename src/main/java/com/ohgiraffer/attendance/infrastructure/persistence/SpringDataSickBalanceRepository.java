package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface SpringDataSickBalanceRepository extends JpaRepository<SickBalanceJpaEntity, Long> {

    Optional<SickBalanceJpaEntity> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE SickBalanceJpaEntity s
    SET s.usedDays = s.usedDays + :amount
    WHERE s.userId = :userId
      AND (s.totalDays - s.usedDays) >= :amount
    """)
    int tryConsume(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}