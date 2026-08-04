package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.UpdateSurveyFormResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record UpdateSurveyFormResponse(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String editUrl,
        Instant updatedAt
) {

    public static UpdateSurveyFormResponse from(
            UpdateSurveyFormResult result
    ) {
        return new UpdateSurveyFormResponse(
                result.surveyFormId(),
                result.title(),
                result.dueAt(),
                result.status(),
                result.editUrl(),
                result.updatedAt()
        );
    }
}