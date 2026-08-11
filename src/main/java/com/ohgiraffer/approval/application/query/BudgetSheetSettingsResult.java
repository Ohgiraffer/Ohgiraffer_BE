package com.ohgiraffer.approval.application.query;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;

import java.time.LocalDateTime;

public record BudgetSheetSettingsResult(
        String spreadsheetUrl,
        String sheetName,
        BudgetColumnMapping columnMapping,
        LocalDateTime lastSyncedAt
) {
}