package com.ohgiraffer.approval.infrastructure.google;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.ExternalSheetPort;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.SheetColumn;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BudgetGoogleSheetAdapter implements BudgetSheetPort, ExternalSheetPort {

    private static final int REQUIRED_BUDGET_MAPPING_COUNT = 4;

    private final GoogleSheetsClient googleSheetsClient;
    private final SpreadsheetIdExtractor spreadsheetIdExtractor;

    public BudgetGoogleSheetAdapter(
            GoogleSheetsClient googleSheetsClient,
            SpreadsheetIdExtractor spreadsheetIdExtractor
    ) {
        this.googleSheetsClient = googleSheetsClient;
        this.spreadsheetIdExtractor = spreadsheetIdExtractor;
    }

    @Override
    public String extractSpreadsheetId(
            String spreadsheetUrl
    ) {
        return spreadsheetIdExtractor.extract(
                spreadsheetUrl
        );
    }

    @Override
    public String getSpreadsheetTitle(
            String spreadsheetId
    ) {
        return googleSheetsClient.getSpreadsheetTitle(
                spreadsheetId
        );
    }

    @Override
    public List<SheetColumn> getSheetColumns(
            String spreadsheetId
    ) {
        List<String> sheetNames = googleSheetsClient.getSheetNames(
                spreadsheetId
        );

        List<SheetColumn> result = new ArrayList<>();

        for (String sheetName : sheetNames) {
            List<List<Object>> rows = googleSheetsClient.readRange(
                    spreadsheetId,
                    toRange(
                            sheetName
                    )
            );

            result.add(
                    new SheetColumn(
                            sheetName,
                            findColumnCandidates(
                                    rows
                            )
                    )
            );
        }

        return result;
    }

    @Override
    public List<BudgetSheetRow> readBudgetRows(
            String spreadsheetId,
            String sheetName,
            BudgetColumnMapping columnMapping
    ) {
        validateColumnMapping(
                columnMapping
        );

        BudgetColumnMapping normalizedColumnMapping = normalizeColumnMapping(
                columnMapping
        );

        List<List<Object>> rows = googleSheetsClient.readRange(
                spreadsheetId,
                toRange(
                        sheetName
                )
        );

        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        int headerRowIndex = findMappedHeaderRowIndex(
                rows,
                normalizedColumnMapping
        );

        Map<String, Integer> headerIndex = createHeaderIndex(
                rows.get(
                        headerRowIndex
                )
        );

        int categoryIndex = getRequiredColumnIndex(
                headerIndex,
                normalizedColumnMapping.category()
        );
        int totalAmountIndex = getRequiredColumnIndex(
                headerIndex,
                normalizedColumnMapping.totalAmount()
        );
        int usedAmountIndex = getRequiredColumnIndex(
                headerIndex,
                normalizedColumnMapping.usedAmount()
        );
        int remainingAmountIndex = getRequiredColumnIndex(
                headerIndex,
                normalizedColumnMapping.remainingAmount()
        );

        Map<String, BudgetSheetRow> budgetRows = new LinkedHashMap<>();

        for (int rowIndex = headerRowIndex + 1;
             rowIndex < rows.size();
             rowIndex++) {
            List<Object> row = rows.get(
                    rowIndex
            );

            String categoryName = getCellAsString(
                    row,
                    categoryIndex
            ).strip();

            BigDecimal totalAmount = getCellAsAmount(
                    row,
                    totalAmountIndex
            );
            BigDecimal usedAmount = getCellAsAmount(
                    row,
                    usedAmountIndex
            );
            BigDecimal remainingAmount = getCellAsAmount(
                    row,
                    remainingAmountIndex
            );

            if (!isBudgetDataRow(
                    categoryName,
                    totalAmount,
                    usedAmount,
                    remainingAmount
            )) {
                continue;
            }

            BudgetSheetRow budgetSheetRow = new BudgetSheetRow(
                    categoryName,
                    totalAmount,
                    usedAmount,
                    remainingAmount
            );

            mergeBudgetRow(
                    budgetRows,
                    budgetSheetRow
            );
        }

        return new ArrayList<>(
                budgetRows.values()
        );
    }

    private void mergeBudgetRow(
            Map<String, BudgetSheetRow> budgetRows,
            BudgetSheetRow budgetSheetRow
    ) {
        BudgetSheetRow existingRow = budgetRows.get(
                budgetSheetRow.categoryName()
        );

        if (existingRow == null) {
            budgetRows.put(
                    budgetSheetRow.categoryName(),
                    budgetSheetRow
            );
            return;
        }

        budgetRows.put(
                budgetSheetRow.categoryName(),
                new BudgetSheetRow(
                        budgetSheetRow.categoryName(),
                        existingRow.totalAmount().add(
                                budgetSheetRow.totalAmount()
                        ),
                        existingRow.usedAmount().add(
                                budgetSheetRow.usedAmount()
                        ),
                        existingRow.remainingAmount().add(
                                budgetSheetRow.remainingAmount()
                        )
                )
        );
    }

    private boolean isBudgetDataRow(
            String categoryName,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount
    ) {
        if (categoryName == null || categoryName.isBlank()) {
            return false;
        }

        String normalizedCategoryName = categoryName.strip();

        if (normalizedCategoryName.equals("합계")
                || normalizedCategoryName.equals("총계")
                || normalizedCategoryName.equalsIgnoreCase("total")) {
            return false;
        }

        if (totalAmount == null
                || usedAmount == null
                || remainingAmount == null) {
            return false;
        }

        if (totalAmount.signum() < 0
                || usedAmount.signum() < 0
                || remainingAmount.signum() < 0) {
            return false;
        }

        return totalAmount.compareTo(
                usedAmount.add(
                        remainingAmount
                )
        ) == 0;
    }

    private List<String> findColumnCandidates(
            List<List<Object>> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<String> bestColumns = List.of();

        for (List<Object> row : rows) {
            List<String> columns = toStringList(
                    row
            )
                    .stream()
                    .filter(
                            column -> !column.isBlank()
                    )
                    .toList();

            if (columns.size() > bestColumns.size()) {
                bestColumns = columns;
            }
        }

        return bestColumns;
    }

    private int findMappedHeaderRowIndex(
            List<List<Object>> rows,
            BudgetColumnMapping columnMapping
    ) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Integer> headerIndex = createHeaderIndex(
                    rows.get(
                            rowIndex
                    )
            );

            if (headerIndex.containsKey(
                    columnMapping.category()
            )
                    && headerIndex.containsKey(
                    columnMapping.totalAmount()
            )
                    && headerIndex.containsKey(
                    columnMapping.usedAmount()
            )
                    && headerIndex.containsKey(
                    columnMapping.remainingAmount()
            )) {
                return rowIndex;
            }
        }

        throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE,
                "시트에서 예산 컬럼 헤더를 찾을 수 없습니다."
        );
    }

    private void validateColumnMapping(
            BudgetColumnMapping columnMapping
    ) {
        if (columnMapping == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 컬럼 매핑 정보가 올바르지 않습니다."
            );
        }

        List<String> columns = new ArrayList<>();
        columns.add(
                columnMapping.category()
        );
        columns.add(
                columnMapping.totalAmount()
        );
        columns.add(
                columnMapping.usedAmount()
        );
        columns.add(
                columnMapping.remainingAmount()
        );

        boolean hasBlankColumn = columns.stream()
                .anyMatch(
                        this::isBlank
                );

        if (hasBlankColumn) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 컬럼 매핑 정보가 올바르지 않습니다."
            );
        }

        long uniqueColumnCount = columns.stream()
                .map(
                        String::strip
                )
                .distinct()
                .count();

        if (uniqueColumnCount != REQUIRED_BUDGET_MAPPING_COUNT) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 컬럼은 서로 다른 값으로 매핑해야 합니다."
            );
        }
    }

    private BudgetColumnMapping normalizeColumnMapping(
            BudgetColumnMapping columnMapping
    ) {
        return new BudgetColumnMapping(
                columnMapping.category().strip(),
                columnMapping.totalAmount().strip(),
                columnMapping.usedAmount().strip(),
                columnMapping.remainingAmount().strip()
        );
    }

    private String toRange(
            String sheetName
    ) {
        if (sheetName == null || sheetName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트명은 필수입니다."
            );
        }

        return "'" + sheetName.replace(
                "'",
                "''"
        ) + "'";
    }

    private List<String> toStringList(
            List<Object> row
    ) {
        if (row == null || row.isEmpty()) {
            return List.of();
        }

        return row.stream()
                .map(
                        value -> value == null
                                ? ""
                                : value.toString().strip()
                )
                .toList();
    }

    private Map<String, Integer> createHeaderIndex(
            List<Object> headerRow
    ) {
        Map<String, Integer> headerIndex = new LinkedHashMap<>();

        if (headerRow == null || headerRow.isEmpty()) {
            return headerIndex;
        }

        for (int index = 0; index < headerRow.size(); index++) {
            Object value = headerRow.get(
                    index
            );

            if (value == null) {
                continue;
            }

            String header = value.toString()
                    .strip();

            if (!header.isBlank()) {
                headerIndex.putIfAbsent(
                        header,
                        index
                );
            }
        }

        return headerIndex;
    }

    private int getRequiredColumnIndex(
            Map<String, Integer> headerIndex,
            String columnName
    ) {
        Integer index = headerIndex.get(
                columnName.strip()
        );

        if (index == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트에서 매핑된 컬럼을 찾을 수 없습니다: " + columnName
            );
        }

        return index;
    }

    private String getCellAsString(
            List<Object> row,
            int index
    ) {
        if (row == null
                || index >= row.size()
                || row.get(
                index
        ) == null) {
            return "";
        }

        return row.get(
                index
        ).toString().strip();
    }

    private BigDecimal getCellAsAmount(
            List<Object> row,
            int index
    ) {
        String value = getCellAsString(
                row,
                index
        );

        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalized = value
                .replace(
                        ",",
                        ""
                )
                .replace(
                        "₩",
                        ""
                )
                .replace(
                        "원",
                        ""
                )
                .strip();

        try {
            return new BigDecimal(
                    normalized
            );

        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 금액 형식이 올바르지 않습니다: " + value
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}