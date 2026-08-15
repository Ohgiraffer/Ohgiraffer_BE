package com.ohgiraffer.ai.domain.dto;

import java.time.LocalDateTime;

public record AiUsageStatusResult(
        long totalCallsToday,
        long failCallsToday,
        LocalDateTime lastCallAt,
        Boolean lastCallSuccess,
        String lastFailReason,
        String diagnosis
) {
}