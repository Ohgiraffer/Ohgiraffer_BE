package com.ohgiraffer.approval.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.BudgetAllocationRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetSheetSyncPersistenceService {

    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void persist(
            String sheetUrl,
            String tabName,
            BudgetColumnMapping columnMapping,
            List<BudgetSheetRow> aggregatedRows,
            LocalDateTime syncedAt
    ) {
        saveOrUpdateExternalSheetLink(
                sheetUrl,
                tabName,
                columnMapping,
                syncedAt
        );

        for (BudgetSheetRow row : aggregatedRows) {
            BudgetCategory category = saveOrUpdateCategory(
                    row.categoryName()
            );

            saveOrUpdateAllocation(
                    category.getId(),
                    row.totalAmount(),
                    row.usedAmount(),
                    row.remainingAmount(),
                    syncedAt
            );
        }
    }

    private void saveOrUpdateExternalSheetLink(
            String sheetUrl,
            String tabName,
            BudgetColumnMapping columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        String columnMappingJson = toColumnMappingJson(
                columnMapping
        );

        ExternalSheetLink externalSheetLink = externalSheetLinkRepository.findByDomain(
                        ExternalSheetLink.budgetDomain()
                )
                .orElseGet(() -> ExternalSheetLink.createBudgetLink(
                        sheetUrl,
                        tabName,
                        columnMappingJson,
                        lastSyncedAt
                ));

        externalSheetLink.update(
                sheetUrl,
                tabName,
                columnMappingJson,
                lastSyncedAt
        );

        externalSheetLinkRepository.save(
                externalSheetLink
        );
    }

    private String toColumnMappingJson(
            BudgetColumnMapping columnMapping
    ) {
        try {
            return objectMapper.writeValueAsString(
                    columnMapping
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private BudgetCategory saveOrUpdateCategory(
            String categoryName
    ) {
        BudgetCategory category = budgetCategoryRepository.findByName(
                        categoryName
                )
                .orElseGet(() -> BudgetCategory.createFromSheet(
                        categoryName
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
}