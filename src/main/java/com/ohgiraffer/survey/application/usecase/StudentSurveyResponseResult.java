package com.ohgiraffer.survey.application.usecase;

import java.time.Instant;

public record StudentSurveyResponseResult(
        Long userId,
        String name,
        String email,
        boolean responded,
        Instant submittedAt
) {
}