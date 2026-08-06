package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetSurveyResponsesUseCase {

    SurveyResponseDetailResult
    getSurveyResponses(
            Long surveyFormId,
            String keyword,
            SurveyResponseStatus responseStatus,
            int page,
            int size,
            Long requesterId,
            Role requesterRole
    );
}