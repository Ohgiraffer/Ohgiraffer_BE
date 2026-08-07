package com.ohgiraffer.global.google.sheets;

import java.util.List;

public interface ExternalSheetPort {

    String extractSpreadsheetId(
            String spreadsheetUrl
    );

    String getSpreadsheetTitle(
            String spreadsheetId
    );

    List<SheetColumn> getSheetColumns(
            String spreadsheetId
    );
}