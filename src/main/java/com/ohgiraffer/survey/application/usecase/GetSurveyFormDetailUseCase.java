package com.ohgiraffer.survey.application.usecase;

public interface GetSurveyFormDetailUseCase {

    SurveyFormDetailResult getSurveyForm(
            Long surveyFormId
    );
}