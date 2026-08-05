package com.ohgiraffer.survey.application.usecase;

public interface GetSurveyResponsesUseCase {

    SurveyResponseDetailResult getSurveyResponses(
            Long surveyFormId,
            String keyword,
            SurveyResponseStatus responseStatus,
            int page,
            int size
    );
}