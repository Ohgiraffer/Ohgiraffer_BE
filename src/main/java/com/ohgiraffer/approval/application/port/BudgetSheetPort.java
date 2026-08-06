package com.ohgiraffer.approval.application.port;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.query.BudgetSheetColumn;

import java.util.List;

public interface BudgetSheetPort {

    String extractSpreadsheetId(
            String spreadsheetUrl
    );

    String getSpreadsheetTitle(
            String spreadsheetId
    );

    List<BudgetSheetColumn> getSheetColumns(
            String spreadsheetId
    );

    List<BudgetSheetRow> readBudgetRows(
            String spreadsheetId,
            String sheetName,
            BudgetColumnMapping columnMapping
    );
}