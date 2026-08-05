package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.usecase.DeleteSurveyFormUseCase;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteSurveyFormService
        implements DeleteSurveyFormUseCase {

    private final SurveyFormRepository surveyFormRepository;
    private final SurveyFormPersistenceService persistenceService;
    private final GoogleFormPort googleFormPort;

    @Override
    public void delete(
            Long surveyFormId
    ) {
        validateSurveyFormId(
                surveyFormId
        );

        SurveyForm surveyForm =
                surveyFormRepository
                        .findById(surveyFormId)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.SURVEY_FORM_NOT_FOUND
                                )
                        );

        validateNoResponses(
                surveyForm
        );

        boolean movedToTrash =
                googleFormPort.moveToTrash(
                        surveyForm.getGoogleFormId()
                );

        try {
            persistenceService.delete(
                    surveyForm
            );

        } catch (RuntimeException deleteException) {
            restoreGoogleForm(
                    surveyForm,
                    movedToTrash,
                    deleteException
            );

            throw deleteException;
        }
    }

    private void validateNoResponses(
            SurveyForm surveyForm
    ) {
        boolean hasResponses =
                googleFormPort.hasResponses(
                        surveyForm.getGoogleFormId()
                );

        if (hasResponses) {
            throw new BusinessException(
                    ErrorCode.SURVEY_FORM_HAS_RESPONSES
            );
        }
    }

    private void validateSurveyFormId(
            Long surveyFormId
    ) {
        if (surveyFormId == null
                || surveyFormId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void restoreGoogleForm(
            SurveyForm surveyForm,
            boolean movedToTrash,
            RuntimeException deleteException
    ) {
        if (!movedToTrash) {
            return;
        }

        try {
            googleFormPort.restoreFromTrash(
                    surveyForm.getGoogleFormId()
            );

        } catch (RuntimeException restoreException) {
            log.error(
                    "설문 DB 삭제 실패 후 Google Form 복구에도 실패했습니다. "
                            + "surveyFormId={}, googleFormId={}",
                    surveyForm.getId(),
                    surveyForm.getGoogleFormId(),
                    restoreException
            );

            deleteException.addSuppressed(
                    restoreException
            );
        }
    }
}