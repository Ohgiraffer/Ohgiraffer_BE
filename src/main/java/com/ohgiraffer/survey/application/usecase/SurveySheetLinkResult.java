package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;

import java.time.Instant;

public record SurveySheetLinkResult(
        boolean connected,
        String spreadsheetUrl,
        String spreadsheetId,
        String spreadsheetTitle,
        Long sheetGid,
        String sheetName,
        String respondentColumn,
        String submittedAtColumn,
        Instant linkedAt
) {

    public static SurveySheetLinkResult from(
            SurveySheetLink sheetLink
    ) {
        Instant linkedAt =
                sheetLink.getUpdatedAt() != null
                        ? sheetLink.getUpdatedAt()
                        : sheetLink.getCreatedAt();

        return new SurveySheetLinkResult(
                true,
                sheetLink.getSpreadsheetUrl(),
                sheetLink.getSpreadsheetId(),
                sheetLink.getSpreadsheetTitle(),
                sheetLink.getSheetGid(),
                sheetLink.getSheetName(),
                sheetLink.getRespondentColumn(),
                sheetLink.getSubmittedAtColumn(),
                linkedAt
        );
    }
}