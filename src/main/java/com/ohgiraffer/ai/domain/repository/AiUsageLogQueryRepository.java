package com.ohgiraffer.ai.domain.repository;

import com.ohgiraffer.ai.domain.dto.AiUsageLastCall;
import com.ohgiraffer.ai.domain.dto.FailReasonCount;
import com.ohgiraffer.ai.domain.dto.FeatureCallCount;
import com.ohgiraffer.ai.domain.dto.HourlyCallCount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AiUsageLogQueryRepository {

    long countCallsBetween(LocalDateTime start, LocalDateTime end);

    long countFailuresBetween(LocalDateTime start, LocalDateTime end);

    Optional<AiUsageLastCall> findLastCall();

    List<FeatureCallCount> aggregateByFeature(LocalDateTime start, LocalDateTime end);

    List<FailReasonCount> aggregateFailReasons(LocalDateTime start, LocalDateTime end);

    List<HourlyCallCount> aggregateHourly(LocalDateTime start, LocalDateTime end);
}