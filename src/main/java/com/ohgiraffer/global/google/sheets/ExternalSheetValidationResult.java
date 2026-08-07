package com.ohgiraffer.global.google.sheets;

import java.util.List;

public record ExternalSheetValidationResult(
        String spreadsheetId,
        String spreadsheetTitle,
        List<SheetColumn> sheets
) {
}