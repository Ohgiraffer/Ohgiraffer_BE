package com.ohgiraffer.survey.application.port;

import com.ohgiraffer.survey.application.usecase
        .SurveySummaryPreparationResult;

public interface SurveySummaryPdfPort {

    byte[] generate(
            SurveySummaryPreparationResult result
    );
}