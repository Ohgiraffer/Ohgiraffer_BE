package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetSurveyFormListUseCase {

    List<SurveyFormListResult> getSurveyForms(
            Long userId,
            String userEmail,
            Role role
    );
}