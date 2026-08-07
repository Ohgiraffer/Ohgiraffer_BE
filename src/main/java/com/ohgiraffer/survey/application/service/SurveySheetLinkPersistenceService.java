package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;
import com.ohgiraffer.survey.domain.repository.SurveySheetLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SurveySheetLinkPersistenceService {

    private final SurveySheetLinkRepository surveySheetLinkRepository;

    public SurveySheetLinkPersistenceService(
            SurveySheetLinkRepository surveySheetLinkRepository
    ) {
        this.surveySheetLinkRepository =
                surveySheetLinkRepository;
    }

    @Transactional(readOnly = true)
    public Optional<SurveySheetLink> findBySurveyFormId(
            Long surveyFormId
    ) {
        return surveySheetLinkRepository
                .findBySurveyFormId(
                        surveyFormId
                );
    }

    @Transactional
    public SurveySheetLink save(
            SurveySheetLink surveySheetLink
    ) {
        return surveySheetLinkRepository.save(
                surveySheetLink
        );
    }
}