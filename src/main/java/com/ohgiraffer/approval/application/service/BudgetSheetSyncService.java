package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.global.aop.ratelimit.RateLimited;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class BudgetSheetSyncService {

    private static final int REQUIRED_MAPPING_COUNT = 4;
    private static final ReentrantLock SYNC_LOCK = new ReentrantLock();

    private final BudgetSheetPort budgetSheetPort;
    private final SpreadsheetIdExtractor spreadsheetIdExtractor;
    private final BudgetSheetSyncPersistenceService budgetSheetSyncPersistenceService;
    private final Clock clock;

    @RateLimited(key = "google_sheets_sync", limit = 10, windowSeconds = 60)
    public BudgetSyncResult sync(
            String sheetUrl,
            String tabName,
            BudgetColumnMapping columnMapping
    ) {
        SYNC_LOCK.lock();

        try {
            return doSync(
                    sheetUrl,
                    tabName,
                    columnMapping
            );
        } finally {
            SYNC_LOCK.unlock();
        }
    }

    private BudgetSyncResult doSync(
            String sheetUrl,
            String tabName,
            BudgetColumnMapping columnMapping
    ) {
        validateSyncRequest(
                sheetUrl,
                tabName,
                columnMapping
        );

        LocalDateTime now = LocalDateTime.now(
                clock
        );

        String normalizedSheetUrl = sheetUrl.strip();
        String normalizedTabName = tabName.strip();

        String spreadsheetId = spreadsheetIdExtractor.extract(
                normalizedSheetUrl
        );

        BudgetColumnMapping normalizedColumnMapping = normalizeColumnMapping(
                columnMapping
        );

        List<BudgetSheetRow> rows = budgetSheetPort.readBudgetRows(
                spreadsheetId,
                normalizedTabName,
                normalizedColumnMapping
        );

        List<BudgetSheetRow> aggregatedRows = aggregateRows(
                rows
        );

        if (aggregatedRows.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        budgetSheetSyncPersistenceService.persist(
                normalizedSheetUrl,
                normalizedTabName,
                normalizedColumnMapping,
                aggregatedRows,
                now
        );

        return new BudgetSyncResult(
                aggregatedRows.size(),
                now
        );
    }

    private void validateSyncRequest(
            String sheetUrl,
            String tabName,
            BudgetColumnMapping columnMapping
    ) {
        if (isBlank(
                sheetUrl
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (isBlank(
                tabName
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        validateColumnMapping(
                columnMapping
        );
    }

    private void validateColumnMapping(
            BudgetColumnMapping columnMapping
    ) {
        if (columnMapping == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        List<String> columns = Arrays.asList(
                columnMapping.category(),
                columnMapping.totalAmount(),
                columnMapping.usedAmount(),
                columnMapping.remainingAmount()
        );

        boolean hasBlankColumn = columns.stream()
                .anyMatch(
                        this::isBlank
                );

        if (hasBlankColumn) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        long uniqueColumnCount = columns.stream()
                .map(
                        String::strip
                )
                .distinct()
                .count();

        if (uniqueColumnCount != REQUIRED_MAPPING_COUNT) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
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

    private List<BudgetSheetRow> aggregateRows(
            List<BudgetSheetRow> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Map<String, BudgetSheetRow> aggregatedRows = new LinkedHashMap<>();

        for (BudgetSheetRow row : rows) {
            String normalizedCategoryName = normalizeCategoryName(
                    row.categoryName()
            );

            validateAmounts(
                    row.totalAmount(),
                    row.usedAmount(),
                    row.remainingAmount()
            );

            BudgetSheetRow existingRow = aggregatedRows.get(
                    normalizedCategoryName
            );

            if (existingRow == null) {
                aggregatedRows.put(
                        normalizedCategoryName,
                        new BudgetSheetRow(
                                normalizedCategoryName,
                                row.totalAmount(),
                                row.usedAmount(),
                                row.remainingAmount()
                        )
                );
                continue;
            }

            aggregatedRows.put(
                    normalizedCategoryName,
                    new BudgetSheetRow(
                            normalizedCategoryName,
                            existingRow.totalAmount().add(
                                    row.totalAmount()
                            ),
                            existingRow.usedAmount().add(
                                    row.usedAmount()
                            ),
                            existingRow.remainingAmount().add(
                                    row.remainingAmount()
                            )
                    )
            );
        }

        return new ArrayList<>(
                aggregatedRows.values()
        );
    }

    private String normalizeCategoryName(
            String categoryName
    ) {
        if (isBlank(
                categoryName
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        return categoryName.strip();
    }

    private void validateAmounts(
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount
    ) {
        if (totalAmount == null
                || usedAmount == null
                || remainingAmount == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (totalAmount.signum() < 0
                || usedAmount.signum() < 0
                || remainingAmount.signum() < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (usedAmount.compareTo(
                totalAmount
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (usedAmount.add(
                remainingAmount
        ).compareTo(
                totalAmount
        ) != 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}