package com.ohgiraffer.ai.domain.dto;

public record HourlyCallCount(
        int hour, long successCount, long failCount
) {
}
