package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SurveyFormDetailResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record SurveyFormDetailResponse(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String googleFormId,
        String editUrl,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    public static SurveyFormDetailResponse from(
            SurveyFormDetailResult result
    ) {
        return new SurveyFormDetailResponse(
                result.surveyFormId(),
                result.title(),
                result.dueAt(),
                result.status(),
                result.googleFormId(),
                result.editUrl(),
                result.createdBy(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}