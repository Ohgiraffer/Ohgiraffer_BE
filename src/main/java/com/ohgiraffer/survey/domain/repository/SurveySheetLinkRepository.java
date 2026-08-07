package com.ohgiraffer.survey.domain.repository;

import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;

import java.util.Optional;

public interface SurveySheetLinkRepository {

    SurveySheetLink save(
            SurveySheetLink surveySheetLink
    );

    Optional<SurveySheetLink> findBySurveyFormId(
            Long surveyFormId
    );

    boolean existsBySurveyFormId(
            Long surveyFormId
    );
}