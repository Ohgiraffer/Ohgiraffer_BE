package com.ohgiraffer.ai.domain.dto;

import com.ohgiraffer.ai.domain.model.FailReason;

import java.time.LocalDateTime;

public record AiUsageLastCall(
        LocalDateTime createdAt,
        boolean success,
        FailReason failReason
) {
}