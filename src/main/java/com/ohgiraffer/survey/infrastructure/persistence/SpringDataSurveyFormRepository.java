package com.ohgiraffer.survey.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSurveyFormRepository extends JpaRepository<SurveyFormJpaEntity, Long> {

    List<SurveyFormJpaEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByGoogleFormId(String googleFormId);

}