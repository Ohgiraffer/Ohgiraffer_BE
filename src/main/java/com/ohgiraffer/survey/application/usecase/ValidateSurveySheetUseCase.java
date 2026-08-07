package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface ValidateSurveySheetUseCase {

    SurveySheetValidationResult validate(
            Long surveyFormId,
            String spreadsheetUrl,
            String requestedSheetName,
            Long requesterId,
            Role requesterRole
    );
}