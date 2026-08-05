package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.command.UpdateSurveyFormCommand;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.usecase.UpdateSurveyFormResult;
import com.ohgiraffer.survey.application.usecase.UpdateSurveyFormUseCase;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UpdateSurveyFormService
        implements UpdateSurveyFormUseCase {

    private final SurveyFormRepository surveyFormRepository;
    private final SurveyFormPersistenceService persistenceService;
    private final GoogleFormPort googleFormPort;
    private final Clock clock;

    public UpdateSurveyFormService(
            SurveyFormRepository surveyFormRepository,
            SurveyFormPersistenceService persistenceService,
            GoogleFormPort googleFormPort,
            Clock clock
    ) {
        this.surveyFormRepository = surveyFormRepository;
        this.persistenceService = persistenceService;
        this.googleFormPort = googleFormPort;
        this.clock = clock;
    }

    @Override
    public UpdateSurveyFormResult update(
            UpdateSurveyFormCommand command
    ) {
        validateCommand(command);

        SurveyForm originalSurveyForm =
                surveyFormRepository
                        .findById(command.surveyFormId())
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.SURVEY_FORM_NOT_FOUND
                                )
                        );

        SurveyForm updatedSurveyForm =
                originalSurveyForm.update(
                        command.title(),
                        command.dueAt(),
                        command.status(),
                        LocalDateTime.now(clock)
                );

        boolean statusChanged =
                originalSurveyForm.getStatus()
                        != updatedSurveyForm.getStatus();

        boolean googlePublishStateUpdated = false;

        try {
            if (statusChanged) {
                applyGooglePublishState(
                        originalSurveyForm.getGoogleFormId(),
                        updatedSurveyForm.getStatus()
                );

                googlePublishStateUpdated = true;
            }

            SurveyForm savedSurveyForm =
                    persistenceService.save(
                            updatedSurveyForm
                    );

            return UpdateSurveyFormResult.from(
                    savedSurveyForm
            );

        } catch (RuntimeException exception) {
            compensateGooglePublishState(
                    originalSurveyForm,
                    googlePublishStateUpdated,
                    exception
            );

            throw exception;
        }
    }

    private void validateCommand(
            UpdateSurveyFormCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 수정 요청은 필수입니다."
            );
        }

        if (command.surveyFormId() == null
                || command.surveyFormId() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }
    }

    private void applyGooglePublishState(
            String googleFormId,
            SurveyFormStatus status
    ) {
        switch (status) {
            case DRAFT ->
                    googleFormPort.updatePublishState(
                            googleFormId,
                            false,
                            false
                    );

            case PUBLISHED ->
                    googleFormPort.updatePublishState(
                            googleFormId,
                            true,
                            true
                    );

            case CLOSED ->
                    googleFormPort.updatePublishState(
                            googleFormId,
                            true,
                            false
                    );
        }
    }

    private void compensateGooglePublishState(
            SurveyForm originalSurveyForm,
            boolean publishStateUpdated,
            RuntimeException originalException
    ) {
        if (!publishStateUpdated) {
            return;
        }

        try {
            applyGooglePublishState(
                    originalSurveyForm.getGoogleFormId(),
                    originalSurveyForm.getStatus()
            );

        } catch (RuntimeException compensationException) {
            log.error(
                    "Google Form 게시 상태 복구에 실패했습니다. "
                            + "surveyFormId={}, googleFormId={}, originalStatus={}",
                    originalSurveyForm.getId(),
                    originalSurveyForm.getGoogleFormId(),
                    originalSurveyForm.getStatus(),
                    compensationException
            );

            originalException.addSuppressed(
                    compensationException
            );
        }
    }
}