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
        return getDashboard(LocalDate.now());
    }

    public AiUsageDashboardResult getDashboard(LocalDate targetDate) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();

        LocalDateTime end = targetDate.isEqual(today)
                ? LocalDateTime.now()
                : targetDate.plusDays(1).atStartOfDay();

        var byFeature = aiUsageLogQueryRepository.aggregateByFeature(start, end);
        var failReasons = aiUsageLogQueryRepository.aggregateFailReasons(start, end);
        var hourly = aiUsageLogQueryRepository.aggregateHourly(start, end);

        long totalCalls = byFeature.stream().mapToLong(f -> f.successCount() + f.failCount()).sum();
        long failCount = failReasons.stream().mapToLong(FailReasonCount::count).sum();

        return new AiUsageDashboardResult(targetDate, byFeature, failReasons, hourly, totalCalls, failCount);
    }
}