package com.ohgiraffer.survey.application.command;

import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.LocalDateTime;

public record UpdateSurveyFormCommand(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status
) {
}