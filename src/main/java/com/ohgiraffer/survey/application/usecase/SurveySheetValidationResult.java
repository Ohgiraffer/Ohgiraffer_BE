package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.port.SurveySheetConnectionInfo;

import java.util.List;

public record SurveySheetValidationResult(
        Long surveyFormId,
        String spreadsheetId,
        String spreadsheetTitle,
        List<SheetResult> sheets,
        String selectedSheetName,
        Long selectedSheetGid,
        List<String> columns
) {

    public SurveySheetValidationResult {
        sheets = sheets == null
                ? List.of()
                : List.copyOf(sheets);

        columns = columns == null
                ? List.of()
                : List.copyOf(columns);
    }

    public static SurveySheetValidationResult from(
            Long surveyFormId,
            SurveySheetConnectionInfo connectionInfo
    ) {
        List<SheetResult> sheets =
                connectionInfo.sheets()
                        .stream()
                        .map(sheet ->
                                new SheetResult(
                                        sheet.sheetName(),
                                        sheet.sheetGid()
                                )
                        )
                        .toList();

        return new SurveySheetValidationResult(
                surveyFormId,
                connectionInfo.spreadsheetId(),
                connectionInfo.spreadsheetTitle(),
                sheets,
                connectionInfo.selectedSheetName(),
                connectionInfo.selectedSheetGid(),
                connectionInfo.columns()
        );
    }

    public record SheetResult(
            String sheetName,
            Long sheetGid
    ) {
    }
}