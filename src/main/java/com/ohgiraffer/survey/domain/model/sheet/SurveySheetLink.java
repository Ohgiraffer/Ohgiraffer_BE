package com.ohgiraffer.survey.domain.model.sheet;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

public final class SurveySheetLink {

    private static final int MAX_SPREADSHEET_URL_LENGTH = 500;
    private static final int MAX_SPREADSHEET_ID_LENGTH = 255;
    private static final int MAX_SPREADSHEET_TITLE_LENGTH = 255;
    private static final int MAX_SHEET_NAME_LENGTH = 255;
    private static final int MAX_COLUMN_NAME_LENGTH = 255;

    private final Long id;
    private final Long surveyFormId;
    private final String spreadsheetUrl;
    private final String spreadsheetId;
    private final String spreadsheetTitle;
    private final Long sheetGid;
    private final String sheetName;
    private final String respondentColumn;
    private final String submittedAtColumn;
    private final Long linkedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SurveySheetLink(
            Long id,
            Long surveyFormId,
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.surveyFormId = surveyFormId;
        this.spreadsheetUrl = spreadsheetUrl;
        this.spreadsheetId = spreadsheetId;
        this.spreadsheetTitle = spreadsheetTitle;
        this.sheetGid = sheetGid;
        this.sheetName = sheetName;
        this.respondentColumn = respondentColumn;
        this.submittedAtColumn = submittedAtColumn;
        this.linkedBy = linkedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SurveySheetLink create(
            Long surveyFormId,
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy
    ) {
        validate(
                surveyFormId,
                spreadsheetUrl,
                spreadsheetId,
                spreadsheetTitle,
                sheetGid,
                sheetName,
                respondentColumn,
                submittedAtColumn,
                linkedBy
        );

        return new SurveySheetLink(
                null,
                surveyFormId,
                spreadsheetUrl.trim(),
                spreadsheetId.trim(),
                spreadsheetTitle.trim(),
                sheetGid,
                sheetName,
                respondentColumn.trim(),
                submittedAtColumn.trim(),
                linkedBy,
                null,
                null
        );
    }

    public static SurveySheetLink restore(
            Long id,
            Long surveyFormId,
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 시트 연결 ID가 올바르지 않습니다."
            );
        }

        validate(
                surveyFormId,
                spreadsheetUrl,
                spreadsheetId,
                spreadsheetTitle,
                sheetGid,
                sheetName,
                respondentColumn,
                submittedAtColumn,
                linkedBy
        );

        return new SurveySheetLink(
                id,
                surveyFormId,
                spreadsheetUrl.trim(),
                spreadsheetId.trim(),
                spreadsheetTitle.trim(),
                sheetGid,
                sheetName,
                respondentColumn.trim(),
                submittedAtColumn.trim(),
                linkedBy,
                createdAt,
                updatedAt
        );
    }

    public SurveySheetLink changeConnection(
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy
    ) {
        validate(
                surveyFormId,
                spreadsheetUrl,
                spreadsheetId,
                spreadsheetTitle,
                sheetGid,
                sheetName,
                respondentColumn,
                submittedAtColumn,
                linkedBy
        );

        return new SurveySheetLink(
                id,
                surveyFormId,
                spreadsheetUrl.trim(),
                spreadsheetId.trim(),
                spreadsheetTitle.trim(),
                sheetGid,
                sheetName,
                respondentColumn.trim(),
                submittedAtColumn.trim(),
                linkedBy,
                createdAt,
                updatedAt
        );
    }

    private static void validate(
            Long surveyFormId,
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy
    ) {
        if (surveyFormId == null || surveyFormId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }

        validateText(
                spreadsheetUrl,
                MAX_SPREADSHEET_URL_LENGTH,
                "Google Spreadsheet URL"
        );

        validateText(
                spreadsheetId,
                MAX_SPREADSHEET_ID_LENGTH,
                "Google Spreadsheet ID"
        );

        validateText(
                spreadsheetTitle,
                MAX_SPREADSHEET_TITLE_LENGTH,
                "Google Spreadsheet 제목"
        );

        if (sheetGid == null || sheetGid < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Sheet gid가 올바르지 않습니다."
            );
        }

        validateText(
                sheetName,
                MAX_SHEET_NAME_LENGTH,
                "Google Sheet 이름"
        );

        validateText(
                respondentColumn,
                MAX_COLUMN_NAME_LENGTH,
                "응답자 식별 컬럼"
        );

        validateText(
                submittedAtColumn,
                MAX_COLUMN_NAME_LENGTH,
                "응답 일시 컬럼"
        );

        if (respondentColumn.trim()
                .equals(submittedAtColumn.trim())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "응답자 식별 컬럼과 응답 일시 컬럼은 서로 달라야 합니다."
            );
        }

        if (linkedBy == null || linkedBy <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트 연결 사용자 ID가 올바르지 않습니다."
            );
        }
    }

    private static void validateText(
            String value,
            int maximumLength,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    fieldName + "은(는) 필수입니다."
            );
        }

        if (value.trim().length() > maximumLength) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    fieldName + "은(는) "
                            + maximumLength
                            + "자 이하여야 합니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getSurveyFormId() {
        return surveyFormId;
    }

    public String getSpreadsheetUrl() {
        return spreadsheetUrl;
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    public String getSpreadsheetTitle() {
        return spreadsheetTitle;
    }

    public Long getSheetGid() {
        return sheetGid;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getRespondentColumn() {
        return respondentColumn;
    }

    public String getSubmittedAtColumn() {
        return submittedAtColumn;
    }

    public Long getLinkedBy() {
        return linkedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}