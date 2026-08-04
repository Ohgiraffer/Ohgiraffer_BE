package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.CreateSurveyFormResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record CreateSurveyFormResponse(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String googleFormId,
        String editUrl,
        Instant createdAt
) {

    public static CreateSurveyFormResponse from(
            CreateSurveyFormResult result
    ) {
        return new CreateSurveyFormResponse(
                result.surveyFormId(),
                result.title(),
                result.dueAt(),
                result.status(),
                result.googleFormId(),
                result.editUrl(),
                result.createdAt()
        );
    }
}