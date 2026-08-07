package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GenerateSurveySummaryPdfUseCase {

    SurveySummaryPdfResult generate(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    );
}