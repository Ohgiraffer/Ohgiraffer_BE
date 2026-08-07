package com.ohgiraffer.survey.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSurveySheetLinkRepository
        extends JpaRepository<SurveySheetLinkJpaEntity, Long> {

    Optional<SurveySheetLinkJpaEntity> findBySurveyFormId(
            Long surveyFormId
    );

    boolean existsBySurveyFormId(
            Long surveyFormId
    );
}