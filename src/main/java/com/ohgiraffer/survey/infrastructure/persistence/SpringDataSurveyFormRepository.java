package com.ohgiraffer.survey.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSurveyFormRepository
        extends JpaRepository<
        SurveyFormJpaEntity,
        Long
        > {

    boolean existsByGoogleFormId(
            String googleFormId
    );
}