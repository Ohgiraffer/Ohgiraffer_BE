package com.ohgiraffer.approval.application.usecase;

public interface ValidateBudgetSheetUseCase {

    BudgetSheetValidationResult validate(
            String spreadsheetUrl
    );
}