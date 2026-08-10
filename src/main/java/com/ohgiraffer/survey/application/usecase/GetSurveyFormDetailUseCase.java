package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetSurveyFormDetailUseCase {

    SurveyFormDetailResult getSurveyForm(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    );
}