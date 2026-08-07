package com.ohgiraffer.survey.application.port;

import java.util.List;

public record SurveySheetConnectionInfo(
        String spreadsheetId,
        String spreadsheetTitle,
        List<SurveySheetInfo> sheets,
        String selectedSheetName,
        Long selectedSheetGid,
        List<String> columns
) {

    public SurveySheetConnectionInfo {
        sheets = sheets == null
                ? List.of()
                : List.copyOf(sheets);

        columns = columns == null
                ? List.of()
                : List.copyOf(columns);
    }

    public record SurveySheetInfo(
            String sheetName,
            Long sheetGid
    ) {
    }
}