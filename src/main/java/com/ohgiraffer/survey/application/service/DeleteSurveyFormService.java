package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.usecase.DeleteSurveyFormUseCase;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
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
        validateSurveyFormId(surveyFormId);

        SurveyForm surveyForm =
                surveyFormRepository
                        .findById(surveyFormId)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.SURVEY_FORM_NOT_FOUND
                                )
                        );

        boolean responsesBlocked = false;
        boolean movedToTrash = false;

        try {
            /*
             * 공개된 설문은 응답 확인 전에 먼저 신규 응답을 차단합니다.
             * 이후 응답 여부를 다시 확인하여 확인과 삭제 사이의
             * 경쟁 상태를 방지합니다.
             */
            responsesBlocked =
                    blockResponsesIfPublished(surveyForm);

            validateNoResponses(surveyForm);

            movedToTrash =
                    googleFormPort.moveToTrash(
                            surveyForm.getGoogleFormId()
                    );

            persistenceService.delete(surveyForm);

        } catch (RuntimeException exception) {
            compensateGoogleForm(
                    surveyForm,
                    responsesBlocked,
                    movedToTrash,
                    exception
            );

            throw exception;
        }
    }

    /**
     * 현재 응답을 받고 있는 PUBLISHED 설문만 신규 응답을 차단합니다.
     *
     * @return 실제로 응답 허용 상태를 변경했으면 true
     */
    private boolean blockResponsesIfPublished(
            SurveyForm surveyForm
    ) {
        if (surveyForm.getStatus()
                != SurveyFormStatus.PUBLISHED) {
            return false;
        }

        googleFormPort.updatePublishState(
                surveyForm.getGoogleFormId(),
                true,
                false
        );

        return true;
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

    /**
     * 삭제 과정에서 실패하면 Google Form을 기존 상태로 복구합니다.
     *
     * 복구 순서:
     * 1. 휴지통에서 복원
     * 2. 기존 응답 허용 상태 복원
     */
    private void compensateGoogleForm(
            SurveyForm surveyForm,
            boolean responsesBlocked,
            boolean movedToTrash,
            RuntimeException originalException
    ) {
        if (movedToTrash) {
            restoreFromTrash(
                    surveyForm,
                    originalException
            );
        }

        if (responsesBlocked) {
            restoreResponseAcceptance(
                    surveyForm,
                    originalException
            );
        }
    }

    private void restoreFromTrash(
            SurveyForm surveyForm,
            RuntimeException originalException
    ) {
        try {
            googleFormPort.restoreFromTrash(
                    surveyForm.getGoogleFormId()
            );

        } catch (RuntimeException restoreException) {
            log.error(
                    "설문 삭제 실패 후 Google Form 휴지통 복원에도 실패했습니다. "
                            + "surveyFormId={}, googleFormId={}",
                    surveyForm.getId(),
                    surveyForm.getGoogleFormId(),
                    restoreException
            );

            originalException.addSuppressed(
                    restoreException
            );
        }
    }

    private void restoreResponseAcceptance(
            SurveyForm surveyForm,
            RuntimeException originalException
    ) {
        try {
            googleFormPort.updatePublishState(
                    surveyForm.getGoogleFormId(),
                    true,
                    true
            );

        } catch (RuntimeException restoreException) {
            log.error(
                    "설문 삭제 실패 후 Google Form 응답 허용 상태 복원에도 실패했습니다. "
                            + "surveyFormId={}, googleFormId={}",
                    surveyForm.getId(),
                    surveyForm.getGoogleFormId(),
                    restoreException
            );

            originalException.addSuppressed(
                    restoreException
            );
        }
    }
}