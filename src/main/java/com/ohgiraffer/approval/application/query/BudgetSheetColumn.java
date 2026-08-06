package com.ohgiraffer.approval.application.query;

import java.util.List;

public record BudgetSheetColumn(
        String sheetName,
        List<String> columns
) {
}