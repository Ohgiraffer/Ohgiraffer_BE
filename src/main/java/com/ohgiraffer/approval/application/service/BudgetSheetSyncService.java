package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.repository.BudgetAllocationRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetSheetSyncService {

    private final BudgetSheetPort budgetSheetPort;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final SpreadsheetIdExtractor spreadsheetIdExtractor;
    private final Clock clock;

    public BudgetSyncResult sync(
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

        String spreadsheetId = spreadsheetIdExtractor.extract(
                sheetUrl
        );

        BudgetColumnMapping normalizedColumnMapping = normalizeColumnMapping(
                columnMapping
        );

        List<BudgetSheetRow> rows = budgetSheetPort.readBudgetRows(
                spreadsheetId,
                tabName.strip(),
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

        for (BudgetSheetRow row : aggregatedRows) {
            BudgetCategory category = saveOrUpdateCategory(
                    row.categoryName()
            );

            saveOrUpdateAllocation(
                    category.getId(),
                    row.totalAmount(),
                    row.usedAmount(),
                    row.remainingAmount(),
                    now
            );
        }

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

        List<String> columns = List.of(
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

        if (uniqueColumnCount != 4) {
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

    private BudgetCategory saveOrUpdateCategory(
            String categoryName
    ) {
        String normalizedCategoryName = normalizeCategoryName(
                categoryName
        );

        BudgetCategory category = budgetCategoryRepository.findByName(
                        normalizedCategoryName
                )
                .orElseGet(() -> BudgetCategory.createFromSheet(
                        normalizedCategoryName
                ));

        return budgetCategoryRepository.save(
                category
        );
    }

    private void saveOrUpdateAllocation(
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime syncedAt
    ) {
        BudgetAllocation budgetAllocation = budgetAllocationRepository.findByBudgetCategoryId(
                        budgetCategoryId
                )
                .orElseGet(() -> BudgetAllocation.create(
                        budgetCategoryId,
                        totalAmount,
                        usedAmount,
                        remainingAmount,
                        syncedAt
                ));

        budgetAllocation.updateAmounts(
                totalAmount,
                usedAmount,
                remainingAmount,
                syncedAt
        );

        budgetAllocationRepository.save(
                budgetAllocation
        );
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}