package com.ohgiraffer.global.google.sheets.presentation.response;

import com.ohgiraffer.global.google.sheets.ExternalSheetValidationResult;
import com.ohgiraffer.global.google.sheets.SheetColumn;

import java.util.List;

public record ExternalSheetValidationResponse(
        String spreadsheetId,
        String spreadsheetTitle,
        List<SheetColumnResponse> sheets
) {

    public static ExternalSheetValidationResponse from(
            ExternalSheetValidationResult result
    ) {
        return new ExternalSheetValidationResponse(
                result.spreadsheetId(),
                result.spreadsheetTitle(),
                result.sheets()
                        .stream()
                        .map(
                                SheetColumnResponse::from
                        )
                        .toList()
        );
    }

    public record SheetColumnResponse(
            String sheetName,
            List<String> columns
    ) {

        public static SheetColumnResponse from(
                SheetColumn sheetColumn
        ) {
            return new SheetColumnResponse(
                    sheetColumn.sheetName(),
                    sheetColumn.columns()
            );
        }
    }
}