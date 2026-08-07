package com.ohgiraffer.survey.application.command;

public record SaveSurveySheetLinkCommand(
        Long surveyFormId,
        String spreadsheetUrl,
        String sheetName,
        String respondentColumn,
        String submittedAtColumn
) {
}