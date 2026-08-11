package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.BudgetSheetSettingsResult;

import java.time.LocalDateTime;

public record BudgetSheetSettingsResponse(
        String spreadsheetUrl,
        String sheetName,
        BudgetColumnMappingResponse columnMapping,
        LocalDateTime lastSyncedAt
) {

    public static BudgetSheetSettingsResponse from(
            BudgetSheetSettingsResult result
    ) {
        return new BudgetSheetSettingsResponse(
                result.spreadsheetUrl(),
                result.sheetName(),
                new BudgetColumnMappingResponse(
                        result.columnMapping().category(),
                        result.columnMapping().totalAmount(),
                        result.columnMapping().usedAmount(),
                        result.columnMapping().remainingAmount()
                ),
                result.lastSyncedAt()
        );
    }

    public record BudgetColumnMappingResponse(
            String category,
            String totalAmount,
            String usedAmount,
            String remainingAmount
    ) {
    }
}