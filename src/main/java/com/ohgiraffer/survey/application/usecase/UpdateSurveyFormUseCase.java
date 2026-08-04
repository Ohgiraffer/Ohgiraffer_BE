package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.command.UpdateSurveyFormCommand;

public interface UpdateSurveyFormUseCase {

    UpdateSurveyFormResult update(
            UpdateSurveyFormCommand command
    );
}