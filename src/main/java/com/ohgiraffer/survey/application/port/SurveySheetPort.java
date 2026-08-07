package com.ohgiraffer.survey.application.port;

public interface SurveySheetPort {

    SurveySheetConnectionInfo inspect(
            String spreadsheetUrl,
            String requestedSheetName
    );
}