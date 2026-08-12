package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.command.SaveSurveySheetLinkCommand;
import com.ohgiraffer.survey.application.port.SurveySheetConnectionInfo;
import com.ohgiraffer.survey.application.port.SurveySheetPort;
import com.ohgiraffer.survey.application.usecase.SaveSurveySheetLinkResult;
import com.ohgiraffer.survey.application.usecase.SaveSurveySheetLinkUseCase;
import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@Service
public class SaveSurveySheetLinkService
        implements SaveSurveySheetLinkUseCase {

    private final SurveyFormRepository surveyFormRepository;
    private final SurveyFormAccessValidator accessValidator;
    private final SurveySheetPort surveySheetPort;
    private final SurveySheetLinkPersistenceService
            persistenceService;

    public SaveSurveySheetLinkService(
            SurveyFormRepository surveyFormRepository,
            SurveyFormAccessValidator accessValidator,
            SurveySheetPort surveySheetPort,
            SurveySheetLinkPersistenceService persistenceService
    ) {
        this.surveyFormRepository = surveyFormRepository;
        this.accessValidator = accessValidator;
        this.surveySheetPort = surveySheetPort;
        this.persistenceService = persistenceService;
    }

    @Override
    public SaveSurveySheetLinkResult save(
            SaveSurveySheetLinkCommand command,
            Long requesterId,
            Role requesterRole
    ) {
        accessValidator.validateStaffAuthority(
                requesterId,
                requesterRole
        );

        validateCommand(
                command
        );

        ensureSurveyFormExists(
                command.surveyFormId()
        );

        SurveySheetConnectionInfo connectionInfo =
                surveySheetPort.inspect(
                        command.spreadsheetUrl().trim(),
                        command.sheetName()
                );


        SurveySheetLink savedSurveySheetLink =
                saveConnection(
                        command,
                        connectionInfo,
                        requesterId
                );

        return SaveSurveySheetLinkResult.from(
                savedSurveySheetLink
        );
    }

    private SurveySheetLink saveConnection(
            SaveSurveySheetLinkCommand command,
            SurveySheetConnectionInfo connectionInfo,
            Long requesterId
    ) {
        SurveySheetLink surveySheetLink =
                createOrChangeConnection(
                        command,
                        connectionInfo,
                        requesterId
                );

        try {
            return persistenceService.save(
                    surveySheetLink
            );
        } catch (DataIntegrityViolationException exception) {
            /*
             * 같은 설문에 대한 최초 연결 요청이 동시에 실행되면
             * 두 요청 모두 기존 연결이 없다고 조회할 수 있습니다.
             *
             * DB의 survey_form_id UNIQUE 제약조건으로 인해
             * 한 요청만 INSERT에 성공하고 다른 요청은 실패합니다.
             *
             * 실패한 요청은 방금 생성된 기존 연결을 다시 조회한 후
             * 연결 변경으로 한 번만 재시도합니다.
             */
            return retryAsConnectionChange(
                    command,
                    connectionInfo,
                    requesterId,
                    exception
            );
        }
    }

    private SurveySheetLink retryAsConnectionChange(
            SaveSurveySheetLinkCommand command,
            SurveySheetConnectionInfo connectionInfo,
            Long requesterId,
            DataIntegrityViolationException originalException
    ) {
        SurveySheetLink existingLink =
                persistenceService
                        .findBySurveyFormId(
                                command.surveyFormId()
                        )
                        .orElseThrow(
                                () -> originalException
                        );

        SurveySheetLink changedLink =
                existingLink.changeConnection(
                        command.spreadsheetUrl().trim(),
                        connectionInfo.spreadsheetId(),
                        connectionInfo.spreadsheetTitle(),
                        connectionInfo.selectedSheetGid(),
                        connectionInfo.selectedSheetName(),
                        requesterId
                );

        return persistenceService.save(
                changedLink
        );
    }

    private SurveySheetLink createOrChangeConnection(
            SaveSurveySheetLinkCommand command,
            SurveySheetConnectionInfo connectionInfo,
            Long requesterId
    ) {
        Optional<SurveySheetLink> existingLink =
                persistenceService.findBySurveyFormId(
                        command.surveyFormId()
                );

        if (existingLink.isPresent()) {
            return existingLink
                    .get()
                    .changeConnection(
                            command.spreadsheetUrl().trim(),
                            connectionInfo.spreadsheetId(),
                            connectionInfo.spreadsheetTitle(),
                            connectionInfo.selectedSheetGid(),
                            connectionInfo.selectedSheetName(),
                            requesterId
                    );
        }

        return SurveySheetLink.create(
                command.surveyFormId(),
                command.spreadsheetUrl().trim(),
                connectionInfo.spreadsheetId(),
                connectionInfo.spreadsheetTitle(),
                connectionInfo.selectedSheetGid(),
                connectionInfo.selectedSheetName(),
                requesterId
        );
    }

    private void ensureSurveyFormExists(
            Long surveyFormId
    ) {
        if (surveyFormRepository
                .findById(surveyFormId)
                .isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SURVEY_FORM_NOT_FOUND
            );
        }
    }

    private void validateCommand(
            SaveSurveySheetLinkCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Sheet 연결 설정이 필요합니다."
            );
        }

        if (command.surveyFormId() == null
                || command.surveyFormId() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }

        if (command.spreadsheetUrl() == null
                || command.spreadsheetUrl().isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        if (command.sheetName() == null
                || command.sheetName().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "연결할 Google Sheet 이름은 필수입니다."
            );
        }
    }


}