package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;

import java.time.Instant;

public record SaveSurveySheetLinkResult(
        Long surveyFormId,
        String spreadsheetUrl,
        String spreadsheetId,
        String spreadsheetTitle,
        String sheetName,
        Long sheetGid,
        String respondentColumn,
        String submittedAtColumn,
        boolean connected,
        Instant linkedAt
) {

    public static SaveSurveySheetLinkResult from(
            SurveySheetLink surveySheetLink
    ) {
        return new SaveSurveySheetLinkResult(
                surveySheetLink.getSurveyFormId(),
                surveySheetLink.getSpreadsheetUrl(),
                surveySheetLink.getSpreadsheetId(),
                surveySheetLink.getSpreadsheetTitle(),
                surveySheetLink.getSheetName(),
                surveySheetLink.getSheetGid(),
                surveySheetLink.getRespondentColumn(),
                surveySheetLink.getSubmittedAtColumn(),
                true,
                surveySheetLink.getUpdatedAt()
        );
    }
}