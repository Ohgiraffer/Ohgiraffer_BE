package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record CreateSurveyFormResult(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String googleFormId,
        String editUrl,
        Instant createdAt
) {

    public static CreateSurveyFormResult from(
            SurveyForm surveyForm
    ) {
        return new CreateSurveyFormResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                surveyForm.getGoogleFormId(),
                surveyForm.getEditUrl(),
                surveyForm.getCreatedAt()
        );
    }
}