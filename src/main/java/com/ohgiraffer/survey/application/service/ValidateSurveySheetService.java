package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.SurveySheetConnectionInfo;
import com.ohgiraffer.survey.application.port.SurveySheetPort;
import com.ohgiraffer.survey.application.usecase.SurveySheetValidationResult;
import com.ohgiraffer.survey.application.usecase.ValidateSurveySheetUseCase;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Service;

@Service

public class ValidateSurveySheetService
        implements ValidateSurveySheetUseCase {

    private final SurveyFormRepository surveyFormRepository;
    private final SurveyFormAccessValidator accessValidator;
    private final SurveySheetPort surveySheetPort;

    public ValidateSurveySheetService(
            SurveyFormRepository surveyFormRepository,
            SurveyFormAccessValidator accessValidator,
            SurveySheetPort surveySheetPort
    ) {
        this.surveyFormRepository = surveyFormRepository;
        this.accessValidator = accessValidator;
        this.surveySheetPort = surveySheetPort;
    }

    @Override
    public SurveySheetValidationResult validate(
            Long surveyFormId,
            String spreadsheetUrl,
            String requestedSheetName,
            Long requesterId,
            Role requesterRole
    ) {
        accessValidator.validateStaffAuthority(
                requesterId,
                requesterRole
        );

        validateSurveyFormId(
                surveyFormId
        );

        validateSpreadsheetUrl(
                spreadsheetUrl
        );

        ensureSurveyFormExists(
                surveyFormId
        );

        SurveySheetConnectionInfo connectionInfo =
                surveySheetPort.inspect(
                        spreadsheetUrl.trim(),
                        normalizeRequestedSheetName(
                                requestedSheetName
                        )
                );

        return SurveySheetValidationResult.from(
                surveyFormId,
                connectionInfo
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

    private void validateSurveyFormId(
            Long surveyFormId
    ) {
        if (surveyFormId == null
                || surveyFormId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }
    }

    private void validateSpreadsheetUrl(
            String spreadsheetUrl
    ) {
        if (spreadsheetUrl == null
                || spreadsheetUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }
    }

    private String normalizeRequestedSheetName(
            String requestedSheetName
    ) {
        if (requestedSheetName == null
                || requestedSheetName.isBlank()) {
            return null;
        }

        return requestedSheetName.trim();
    }
}