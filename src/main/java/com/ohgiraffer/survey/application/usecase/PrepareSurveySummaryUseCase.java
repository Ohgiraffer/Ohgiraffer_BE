package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface PrepareSurveySummaryUseCase {

    SurveySummaryPreparationResult prepare(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    );
}