package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record UpdateSurveyFormResult(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String editUrl,
        Instant updatedAt
) {

    public static UpdateSurveyFormResult from(
            SurveyForm surveyForm
    ) {
        return new UpdateSurveyFormResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                surveyForm.getEditUrl(),
                surveyForm.getUpdatedAt()
        );
    }
}