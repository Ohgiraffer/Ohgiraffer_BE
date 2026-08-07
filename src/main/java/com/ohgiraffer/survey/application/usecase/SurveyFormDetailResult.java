package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import com.ohgiraffer.user.domain.model.Role;

import java.time.Instant;
import java.time.LocalDateTime;

public record SurveyFormDetailResult(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String googleFormId,
        String editUrl,
        String responseUrl,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    public static SurveyFormDetailResult from(
            SurveyForm surveyForm,
            Role requesterRole
    ) {
        boolean staff =
                requesterRole == Role.MANAGER
                        || requesterRole == Role.INSTRUCTOR;

        return new SurveyFormDetailResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                staff
                        ? surveyForm.getGoogleFormId()
                        : null,
                staff
                        ? surveyForm.getEditUrl()
                        : null,
                requesterRole == Role.STUDENT
                        ? surveyForm.getResponseUrl()
                        : null,
                staff
                        ? surveyForm.getCreatedBy()
                        : null,
                staff
                        ? surveyForm.getCreatedAt()
                        : null,
                staff
                        ? surveyForm.getUpdatedAt()
                        : null
        );
    }
}