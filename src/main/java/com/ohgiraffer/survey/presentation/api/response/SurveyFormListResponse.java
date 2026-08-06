package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SurveyFormListResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record SurveyFormListResponse(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        int respondedCount,
        int targetCount,
        Instant createdAt
) {

    public static SurveyFormListResponse from(
            SurveyFormListResult result
    ) {
        return new SurveyFormListResponse(
                result.surveyFormId(),
                result.title(),
                result.dueAt(),
                result.status(),
                result.respondedCount(),
                result.targetCount(),
                result.createdAt()
        );
    }
}