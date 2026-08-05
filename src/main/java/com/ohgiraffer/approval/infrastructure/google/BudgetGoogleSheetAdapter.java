package com.ohgiraffer.approval.infrastructure.google;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.approval.application.query.BudgetSheetColumn;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BudgetGoogleSheetAdapter
        implements BudgetSheetPort {

    private static final String DEFAULT_COLUMN_RANGE = "A:Z";
    private static final int MIN_HEADER_COLUMN_COUNT = 3;

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
    public List<BudgetSheetColumn> getSheetColumns(
            String spreadsheetId
    ) {
        List<String> sheetNames =
                googleSheetsClient.getSheetNames(
                        spreadsheetId
                );

        List<BudgetSheetColumn> result =
                new ArrayList<>();

        for (String sheetName : sheetNames) {
            List<List<Object>> rows =
                    googleSheetsClient.readRange(
                            spreadsheetId,
                            toRange(
                                    sheetName,
                                    DEFAULT_COLUMN_RANGE
                            )
                    );

            result.add(
                    new BudgetSheetColumn(
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

        List<List<Object>> rows =
                googleSheetsClient.readRange(
                        spreadsheetId,
                        toRange(
                                sheetName,
                                DEFAULT_COLUMN_RANGE
                        )
                );

        if (rows.isEmpty()) {
            return List.of();
        }

        int headerRowIndex =
                findMappedHeaderRowIndex(
                        rows,
                        columnMapping
                );

        Map<String, Integer> headerIndex =
                createHeaderIndex(
                        rows.get(
                                headerRowIndex
                        )
                );

        int categoryIndex =
                getRequiredColumnIndex(
                        headerIndex,
                        columnMapping.category()
                );
        int totalAmountIndex =
                getRequiredColumnIndex(
                        headerIndex,
                        columnMapping.totalAmount()
                );
        int usedAmountIndex =
                getRequiredColumnIndex(
                        headerIndex,
                        columnMapping.usedAmount()
                );
        int remainingAmountIndex =
                getRequiredColumnIndex(
                        headerIndex,
                        columnMapping.remainingAmount()
                );

        List<BudgetSheetRow> result =
                new ArrayList<>();

        for (int rowIndex = headerRowIndex + 1;
             rowIndex < rows.size();
             rowIndex++) {
            List<Object> row =
                    rows.get(
                            rowIndex
                    );

            String categoryName =
                    getCellAsString(
                            row,
                            categoryIndex
                    );

            if (categoryName.isBlank()) {
                continue;
            }

            result.add(
                    new BudgetSheetRow(
                            categoryName,
                            getCellAsAmount(
                                    row,
                                    totalAmountIndex
                            ),
                            getCellAsAmount(
                                    row,
                                    usedAmountIndex
                            ),
                            getCellAsAmount(
                                    row,
                                    remainingAmountIndex
                            )
                    )
            );
        }

        return result;
    }

    private List<String> findColumnCandidates(
            List<List<Object>> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        for (List<Object> row : rows) {
            List<String> columns =
                    toStringList(
                            row
                    )
                            .stream()
                            .filter(
                                    column -> !column.isBlank()
                            )
                            .toList();

            if (columns.size() >= MIN_HEADER_COLUMN_COUNT) {
                return columns;
            }
        }

        return List.of();
    }

    private int findMappedHeaderRowIndex(
            List<List<Object>> rows,
            BudgetColumnMapping columnMapping
    ) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Integer> headerIndex =
                    createHeaderIndex(
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
        if (columnMapping == null
                || isBlank(
                columnMapping.category()
        )
                || isBlank(
                columnMapping.totalAmount()
        )
                || isBlank(
                columnMapping.usedAmount()
        )
                || isBlank(
                columnMapping.remainingAmount()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 컬럼 매핑 정보가 올바르지 않습니다."
            );
        }
    }

    private String toRange(
            String sheetName,
            String range
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
        ) + "'!" + range;
    }

    private List<String> toStringList(
            List<Object> row
    ) {
        return row
                .stream()
                .map(
                        value -> value == null
                                ? ""
                                : value
                                .toString()
                                .trim()
                )
                .toList();
    }

    private Map<String, Integer> createHeaderIndex(
            List<Object> headerRow
    ) {
        Map<String, Integer> headerIndex =
                new LinkedHashMap<>();

        for (int index = 0; index < headerRow.size(); index++) {
            Object value =
                    headerRow.get(
                            index
                    );

            if (value == null) {
                continue;
            }

            String header =
                    value
                            .toString()
                            .trim();

            if (!header.isBlank()) {
                headerIndex.put(
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
        Integer index =
                headerIndex.get(
                        columnName
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
        if (index >= row.size()
                || row.get(
                index
        ) == null) {
            return "";
        }

        return row
                .get(
                        index
                )
                .toString()
                .trim();
    }

    private BigDecimal getCellAsAmount(
            List<Object> row,
            int index
    ) {
        String value =
                getCellAsString(
                        row,
                        index
                );

        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalized =
                value
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
                        .trim();

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