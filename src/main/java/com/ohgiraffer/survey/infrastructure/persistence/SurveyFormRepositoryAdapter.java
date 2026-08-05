package com.ohgiraffer.survey.infrastructure.persistence;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public class SurveyFormRepositoryAdapter implements SurveyFormRepository {

    private final SpringDataSurveyFormRepository repository;

    public SurveyFormRepositoryAdapter(SpringDataSurveyFormRepository repository) {
        this.repository = repository;
    }

    @Override
    public SurveyForm save(SurveyForm surveyForm) {
        SurveyFormJpaEntity entity = SurveyFormJpaEntity.from(surveyForm);

        SurveyFormJpaEntity savedEntity = repository.saveAndFlush(entity);

        return savedEntity.toDomain();
    }

    @Override
    public List<SurveyForm> findAll() {return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(SurveyFormJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<SurveyForm> findById(Long surveyFormId) {
        return repository
                .findById(surveyFormId)
                .map(SurveyFormJpaEntity::toDomain);
    }

    @Override
    public boolean existsByGoogleFormId(String googleFormId) {
        return repository.existsByGoogleFormId(googleFormId);
    }

    @Override
    public void delete(SurveyForm surveyForm) {
        repository.deleteById(surveyForm.getId());
        repository.flush();
    }


}