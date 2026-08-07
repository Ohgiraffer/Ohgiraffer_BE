package com.ohgiraffer.survey.infrastructure.persistence;

import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;
import com.ohgiraffer.survey.domain.repository.SurveySheetLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SurveySheetLinkRepositoryAdapter
        implements SurveySheetLinkRepository {

    private final SpringDataSurveySheetLinkRepository repository;

    public SurveySheetLinkRepositoryAdapter(
            SpringDataSurveySheetLinkRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public SurveySheetLink save(
            SurveySheetLink surveySheetLink
    ) {
        SurveySheetLinkJpaEntity entity =
                SurveySheetLinkJpaEntity.from(
                        surveySheetLink
                );

        SurveySheetLinkJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<SurveySheetLink> findBySurveyFormId(
            Long surveyFormId
    ) {
        return repository
                .findBySurveyFormId(
                        surveyFormId
                )
                .map(
                        SurveySheetLinkJpaEntity::toDomain
                );
    }

    @Override
    public boolean existsBySurveyFormId(
            Long surveyFormId
    ) {
        return repository.existsBySurveyFormId(
                surveyFormId
        );
    }
}