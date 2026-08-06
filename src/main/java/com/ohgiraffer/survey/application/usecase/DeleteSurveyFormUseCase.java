package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface DeleteSurveyFormUseCase {

    void delete(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    );
}