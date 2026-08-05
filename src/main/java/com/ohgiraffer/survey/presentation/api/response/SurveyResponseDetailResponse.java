package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SurveyResponseDetailResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResponseDetailResponse(
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
        List<StudentSurveyResponse> students
) {

    public static SurveyResponseDetailResponse from(
            SurveyResponseDetailResult result
    ) {
        return new SurveyResponseDetailResponse(
                result.surveyFormId(),
                result.title(),
                result.surveyStatus(),
                result.dueAt(),
                result.respondedCount(),
                result.targetCount(),
                result.page(),
                result.size(),
                result.filteredCount(),
                result.totalPages(),
                result.students()
                        .stream()
                        .map(StudentSurveyResponse::from)
                        .toList()
        );
    }
}