package com.ohgiraffer.ai.application.service;

import com.ohgiraffer.ai.domain.dto.AiUsageDashboardResult;
import com.ohgiraffer.ai.domain.dto.FailReasonCount;
import com.ohgiraffer.ai.domain.repository.AiUsageLogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiUsageDashboardQueryService {

    private final AiUsageLogQueryRepository aiUsageLogQueryRepository;

    public AiUsageDashboardResult getTodayDashboard() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        var byFeature = aiUsageLogQueryRepository.aggregateByFeature(start, end);
        var failReasons = aiUsageLogQueryRepository.aggregateFailReasons(start, end);
        var hourly = aiUsageLogQueryRepository.aggregateHourly(start, end);

        long totalCalls = byFeature.stream().mapToLong(f -> f.successCount() + f.failCount()).sum();
        long failCount = failReasons.stream().mapToLong(FailReasonCount::count).sum();

        return new AiUsageDashboardResult(LocalDate.now(), byFeature, failReasons, hourly, totalCalls, failCount);
    }
}