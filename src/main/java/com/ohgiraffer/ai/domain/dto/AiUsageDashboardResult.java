package com.ohgiraffer.ai.domain.dto;

import java.time.LocalDate;
import java.util.List;

public record AiUsageDashboardResult(
        LocalDate today,
        List<FeatureCallCount> byFeature,
        List<FailReasonCount> failReasons,
        List<HourlyCallCount> hourly,
        long totalCalls,
        long failCount
) {
}
