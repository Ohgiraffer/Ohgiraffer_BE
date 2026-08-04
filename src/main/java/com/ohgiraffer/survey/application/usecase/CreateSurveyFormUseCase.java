package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.command.CreateSurveyFormCommand;

public interface CreateSurveyFormUseCase {

    CreateSurveyFormResult create(
            CreateSurveyFormCommand command
    );
}