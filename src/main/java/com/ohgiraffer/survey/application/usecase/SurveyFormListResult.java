package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record SurveyFormListResult(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        int respondedCount,
        int targetCount,
        Instant createdAt
) {

    public static SurveyFormListResult from(
            SurveyForm surveyForm,
            int respondedCount,
            int targetCount
    ) {
        return new SurveyFormListResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                respondedCount,
                targetCount,
                surveyForm.getCreatedAt()
        );
    }
}