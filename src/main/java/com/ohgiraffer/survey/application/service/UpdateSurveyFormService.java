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
import java.util.Objects;

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

        boolean titleChanged =
                !Objects.equals(
                        originalSurveyForm.getTitle(),
                        updatedSurveyForm.getTitle()
                );

        boolean statusChanged =
                originalSurveyForm.getStatus()
                        != updatedSurveyForm.getStatus();

        boolean googleTitleUpdated = false;
        boolean googlePublishStateUpdated = false;

        try {
            if (titleChanged) {
                googleFormPort.updateTitle(
                        originalSurveyForm.getGoogleFormId(),
                        updatedSurveyForm.getTitle()
                );

                googleTitleUpdated = true;
            }

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
            compensateGoogleFormUpdate(
                    originalSurveyForm,
                    googleTitleUpdated,
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

    private void compensateGoogleFormUpdate(
            SurveyForm originalSurveyForm,
            boolean titleUpdated,
            boolean publishStateUpdated,
            RuntimeException originalException
    ) {
        /*
         * 상태를 먼저 되돌리고 제목을 되돌립니다.
         * 보상 처리 중 발생한 오류는 원래 오류를 덮지 않고
         * suppressed exception으로 보관합니다.
         */
        if (publishStateUpdated) {
            try {
                applyGooglePublishState(
                        originalSurveyForm.getGoogleFormId(),
                        originalSurveyForm.getStatus()
                );
            } catch (RuntimeException compensationException) {
                originalException.addSuppressed(
                        compensationException
                );
            }
        }

        if (titleUpdated) {
            try {
                googleFormPort.updateTitle(
                        originalSurveyForm.getGoogleFormId(),
                        originalSurveyForm.getTitle()
                );
            } catch (RuntimeException compensationException) {
                originalException.addSuppressed(
                        compensationException
                );
            }
        }
    }
}