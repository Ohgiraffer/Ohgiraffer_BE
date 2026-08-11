package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.SaveRecordResult;

public record SaveRecordResponse(
        boolean aiBriefGenerated,
        String message
) {
    public static SaveRecordResponse from(SaveRecordResult result) {
        return new SaveRecordResponse(result.aiBriefGenerated(), result.message());
    }
}