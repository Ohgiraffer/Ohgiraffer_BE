package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.BudgetSheetColumn;

import java.util.List;

public record BudgetSheetValidationResult(
        String spreadsheetId,
        String spreadsheetTitle,
        List<BudgetSheetColumn> sheets
) {
}