package com.ohgiraffer.survey.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataSurveyFormRepository extends JpaRepository<SurveyFormJpaEntity, Long> {

    List<SurveyFormJpaEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByGoogleFormId(String googleFormId);

    @Query("""
        SELECT surveyForm
        FROM SurveyFormJpaEntity surveyForm
        JOIN UserJpaEntity creator
          ON creator.id = surveyForm.createdBy
        WHERE creator.bootcampId = :bootcampId
        ORDER BY surveyForm.createdAt DESC
        """)
    List<SurveyFormJpaEntity> findAllByBootcampIdOrderByCreatedAtDesc(
            @Param("bootcampId") Long bootcampId
    );

}