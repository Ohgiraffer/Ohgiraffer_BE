package com.ohgiraffer.survey.application.port;

public interface SurveyResponseDataPort {

    SurveyResponseDataset readResponses(
            String spreadsheetId,
            String spreadsheetTitle,
            String sheetName
    );
}