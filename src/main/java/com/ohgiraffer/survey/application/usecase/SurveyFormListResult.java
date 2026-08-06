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
        Boolean responded,
        String responseUrl,
        Instant createdAt

) {

    public static SurveyFormListResult from(
            SurveyForm surveyForm,
            int respondedCount,
            int targetCount,
            Boolean responded,
            String responseUrl
    ) {
        return new SurveyFormListResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                respondedCount,
                targetCount,
                responded,
                responseUrl,
                surveyForm.getCreatedAt()
        );
    }

    public static SurveyFormListResult from(
            SurveyForm surveyForm,
            int respondedCount,
            int targetCount
    ) {
        return from(
                surveyForm,
                respondedCount,
                targetCount,
                null,
                null
        );
    }
}