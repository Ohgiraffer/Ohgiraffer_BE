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
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = now;

        var byFeature = aiUsageLogQueryRepository.aggregateByFeature(start, end);
        var failReasons = aiUsageLogQueryRepository.aggregateFailReasons(start, end);
        var hourly = aiUsageLogQueryRepository.aggregateHourly(start, end);

        long totalCalls = byFeature.stream().mapToLong(f -> f.successCount() + f.failCount()).sum();
        long failCount = failReasons.stream().mapToLong(FailReasonCount::count).sum();

        return new AiUsageDashboardResult(today, byFeature, failReasons, hourly, totalCalls, failCount);
    }
}