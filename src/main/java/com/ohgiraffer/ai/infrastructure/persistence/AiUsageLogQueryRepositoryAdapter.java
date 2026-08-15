package com.ohgiraffer.ai.infrastructure.persistence;

import com.ohgiraffer.ai.domain.dto.AiUsageLastCall;
import com.ohgiraffer.ai.domain.dto.FailReasonCount;
import com.ohgiraffer.ai.domain.dto.FeatureCallCount;
import com.ohgiraffer.ai.domain.dto.HourlyCallCount;
import com.ohgiraffer.ai.domain.repository.AiUsageLogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiUsageLogQueryRepositoryAdapter implements AiUsageLogQueryRepository {

    private final SpringDataAiUsageLogJpaRepository aiUsageLogJpaRepository;

    @Override
    public long countCallsBetween(LocalDateTime start, LocalDateTime end) {
        return aiUsageLogJpaRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public long countFailuresBetween(LocalDateTime start, LocalDateTime end) {
        return aiUsageLogJpaRepository.countBySuccessFalseAndCreatedAtBetween(start, end);
    }

    @Override
    public Optional<AiUsageLastCall> findLastCall() {
        return aiUsageLogJpaRepository.findTopByOrderByCreatedAtDesc()
                .map(e -> new AiUsageLastCall(e.getCreatedAt(), e.isSuccess(), e.getFailReason()));
    }

    @Override
    public List<FeatureCallCount> aggregateByFeature(LocalDateTime start, LocalDateTime end) {
        return aiUsageLogJpaRepository.aggregateByFeatureToday(start, end);
    }

    @Override
    public List<FailReasonCount> aggregateFailReasons(LocalDateTime start, LocalDateTime end) {
        return aiUsageLogJpaRepository.aggregateFailReasonsToday(start, end);
    }

    @Override
    public List<HourlyCallCount> aggregateHourly(LocalDateTime start, LocalDateTime end) {
        return aiUsageLogJpaRepository.aggregateHourlyToday(start, end);
    }
}