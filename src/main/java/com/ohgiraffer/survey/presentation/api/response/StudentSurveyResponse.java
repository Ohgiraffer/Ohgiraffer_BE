package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.StudentSurveyResponseResult;

import java.time.Instant;

public record StudentSurveyResponse(
        Long userId,
        String name,
        String email,
        boolean responded,
        Instant submittedAt
) {

    public static StudentSurveyResponse from(
            StudentSurveyResponseResult result
    ) {
        return new StudentSurveyResponse(
                result.userId(),
                result.name(),
                result.email(),
                result.responded(),
                result.submittedAt()
        );
    }
}