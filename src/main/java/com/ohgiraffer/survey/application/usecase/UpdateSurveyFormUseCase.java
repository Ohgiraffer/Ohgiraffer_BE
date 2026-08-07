package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.command
        .UpdateSurveyFormCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface UpdateSurveyFormUseCase {

    UpdateSurveyFormResult update(
            UpdateSurveyFormCommand command,
            Long requesterId,
            Role requesterRole
    );
}