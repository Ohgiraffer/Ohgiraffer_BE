package com.ohgiraffer.global.google.sheets;

import java.util.List;

public record SheetColumn(
        String sheetName,
        List<String> columns
) {
}