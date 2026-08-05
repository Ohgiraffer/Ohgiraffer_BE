package com.ohgiraffer.survey.application.port;

import java.time.Instant;

public record GoogleFormResponseInfo(
        String respondentEmail,
        Instant submittedAt
) {
}