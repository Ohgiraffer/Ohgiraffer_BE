package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record SurveyFormDetailResult(
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

    public static SurveyFormDetailResult from(
            SurveyForm surveyForm
    ) {
        return new SurveyFormDetailResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                surveyForm.getGoogleFormId(),
                surveyForm.getEditUrl(),
                surveyForm.getCreatedBy(),
                surveyForm.getCreatedAt(),
                surveyForm.getUpdatedAt()
        );
    }
}