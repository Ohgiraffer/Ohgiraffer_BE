package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.BudgetSheetColumn;
import com.ohgiraffer.approval.application.usecase.BudgetSheetValidationResult;

import java.util.List;

public record BudgetSheetValidationResponse(
        String spreadsheetId,
        String spreadsheetTitle,
        List<SheetColumnResponse> sheets
) {

    public static BudgetSheetValidationResponse from(
            BudgetSheetValidationResult result
    ) {
        return new BudgetSheetValidationResponse(
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
                BudgetSheetColumn sheetColumn
        ) {
            return new SheetColumnResponse(
                    sheetColumn.sheetName(),
                    sheetColumn.columns()
            );
        }
    }
}