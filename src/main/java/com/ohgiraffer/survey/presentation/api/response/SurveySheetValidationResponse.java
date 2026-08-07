package com.ohgiraffer.survey.presentation.api.response;

import com.ohgiraffer.survey.application.usecase.SurveySheetValidationResult;

import java.util.List;

public record SurveySheetValidationResponse(
        Long surveyFormId,
        String spreadsheetId,
        String spreadsheetTitle,
        List<SheetResponse> sheets,
        String selectedSheetName,
        Long selectedSheetGid,
        List<String> columns
) {

    public SurveySheetValidationResponse {
        sheets = sheets == null
                ? List.of()
                : List.copyOf(sheets);

        columns = columns == null
                ? List.of()
                : List.copyOf(columns);
    }

    public static SurveySheetValidationResponse from(
            SurveySheetValidationResult result
    ) {
        List<SheetResponse> sheets =
                result.sheets()
                        .stream()
                        .map(sheet ->
                                new SheetResponse(
                                        sheet.sheetName(),
                                        sheet.sheetGid()
                                )
                        )
                        .toList();

        return new SurveySheetValidationResponse(
                result.surveyFormId(),
                result.spreadsheetId(),
                result.spreadsheetTitle(),
                sheets,
                result.selectedSheetName(),
                result.selectedSheetGid(),
                result.columns()
        );
    }

    public record SheetResponse(
            String sheetName,
            Long sheetGid
    ) {
    }
}