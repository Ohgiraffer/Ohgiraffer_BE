package com.ohgiraffer.approval.application.command;

public record SaveBudgetSheetSettingsCommand(
        String spreadsheetUrl,
        String sheetName,
        BudgetColumnMapping columnMapping
) {
}