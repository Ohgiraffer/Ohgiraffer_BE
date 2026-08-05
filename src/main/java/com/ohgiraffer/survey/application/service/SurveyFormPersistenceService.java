package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyFormPersistenceService {

    private final SurveyFormRepository surveyFormRepository;

    public SurveyFormPersistenceService(
            SurveyFormRepository surveyFormRepository
    ) {
        this.surveyFormRepository = surveyFormRepository;
    }

    @Transactional
    public SurveyForm save(SurveyForm surveyForm) {
        return surveyFormRepository.save(surveyForm);
    }

    @Transactional
    public void delete(SurveyForm surveyForm) {
        surveyFormRepository.delete(surveyForm);
    }


}