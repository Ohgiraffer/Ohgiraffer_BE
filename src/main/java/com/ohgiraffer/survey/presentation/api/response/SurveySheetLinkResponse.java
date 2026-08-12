package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SurveySheetLinkResult;

import java.time.Instant;

public record SurveySheetLinkResponse(
        boolean connected,
        String spreadsheetUrl,
        String spreadsheetId,
        String spreadsheetTitle,
        Long sheetGid,
        String sheetName,
        Instant linkedAt
) {

    public static SurveySheetLinkResponse from(
            SurveySheetLinkResult result
    ) {
        if (result == null) {
            return null;
        }

        return new SurveySheetLinkResponse(
                result.connected(),
                result.spreadsheetUrl(),
                result.spreadsheetId(),
                result.spreadsheetTitle(),
                result.sheetGid(),
                result.sheetName(),
                result.linkedAt()
        );
    }
}