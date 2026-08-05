package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResponseDetailResult(
        Long surveyFormId,
        String title,
        SurveyFormStatus surveyStatus,
        LocalDateTime dueAt,
        int respondedCount,
        int targetCount,
        int page,
        int size,
        long filteredCount,
        int totalPages,
        List<StudentSurveyResponseResult> students
) {
}