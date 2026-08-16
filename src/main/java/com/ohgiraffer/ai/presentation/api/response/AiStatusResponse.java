package com.ohgiraffer.ai.presentation.api.response;

import java.time.LocalDateTime;

public record AiStatusResponse(
        long totalCallsToday,
        long failCallsToday,
        LocalDateTime lastCallAt,
        Boolean lastCallSuccess,
        String lastFailReason,
        String diagnosis
) {}
