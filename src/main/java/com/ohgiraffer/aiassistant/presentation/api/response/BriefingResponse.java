package com.ohgiraffer.aiassistant.presentation.api.response;

import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;

import java.time.LocalDateTime;

public record BriefingResponse(
        String summaryText,
        LocalDateTime generatedAt
) {

    public static BriefingResponse from(BriefingSummary summary) {
        return new BriefingResponse(summary.summaryText(), summary.generatedAt());
    }

}
