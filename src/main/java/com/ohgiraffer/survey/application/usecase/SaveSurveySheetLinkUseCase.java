package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.command.SaveSurveySheetLinkCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface SaveSurveySheetLinkUseCase {

    SaveSurveySheetLinkResult save(
            SaveSurveySheetLinkCommand command,
            Long requesterId,
            Role requesterRole
    );
}