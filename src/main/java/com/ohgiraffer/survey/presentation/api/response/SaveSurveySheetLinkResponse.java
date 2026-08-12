package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SaveSurveySheetLinkResult;

import java.time.Instant;

public record SaveSurveySheetLinkResponse(
        Long surveyFormId,
        String spreadsheetUrl,
        String spreadsheetId,
        String spreadsheetTitle,
        String sheetName,
        Long sheetGid,
        boolean connected,
        Instant linkedAt
) {

    public static SaveSurveySheetLinkResponse from(
            SaveSurveySheetLinkResult result
    ) {
        return new SaveSurveySheetLinkResponse(
                result.surveyFormId(),
                result.spreadsheetUrl(),
                result.spreadsheetId(),
                result.spreadsheetTitle(),
                result.sheetName(),
                result.sheetGid(),
                result.connected(),
                result.linkedAt()
        );
    }
}