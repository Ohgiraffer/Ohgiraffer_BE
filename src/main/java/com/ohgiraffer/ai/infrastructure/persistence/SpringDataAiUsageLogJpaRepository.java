package com.ohgiraffer.ai.infrastructure.persistence;

import com.ohgiraffer.ai.domain.dto.FailReasonCount;
import com.ohgiraffer.ai.domain.dto.FeatureCallCount;
import com.ohgiraffer.ai.domain.dto.HourlyCallCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataAiUsageLogJpaRepository extends JpaRepository<AiUsageLogEntity, Long> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countBySuccessFalseAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT new com.ohgiraffer.ai.domain.dto.FeatureCallCount(
            a.featureName,
            SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END),
            SUM(CASE WHEN a.success = false THEN 1 ELSE 0 END),
            COALESCE(SUM(a.totalTokens), 0)
        )
        FROM AiUsageLogEntity a
        WHERE a.createdAt >= :start AND a.createdAt < :end
        GROUP BY a.featureName
        ORDER BY COUNT(a) DESC
        """)
    List<FeatureCallCount> aggregateByFeatureToday(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT new com.ohgiraffer.ai.domain.dto.FailReasonCount(
            a.failReason, COUNT(a)
        )
        FROM AiUsageLogEntity a
        WHERE a.success = false AND a.createdAt >= :start AND a.createdAt < :end
        GROUP BY a.failReason
        ORDER BY COUNT(a) DESC
        """)
    List<FailReasonCount> aggregateFailReasonsToday(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT new com.ohgiraffer.ai.domain.dto.HourlyCallCount(
            EXTRACT(HOUR FROM a.createdAt),
            SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END),
            SUM(CASE WHEN a.success = false THEN 1 ELSE 0 END)
        )
        FROM AiUsageLogEntity a
        WHERE a.createdAt >= :start AND a.createdAt < :end
        GROUP BY EXTRACT(HOUR FROM a.createdAt)
        ORDER BY EXTRACT(HOUR FROM a.createdAt)
        """)
    List<HourlyCallCount> aggregateHourlyToday(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM AiUsageLogEntity a WHERE a.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    Optional<AiUsageLogEntity> findTopByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}