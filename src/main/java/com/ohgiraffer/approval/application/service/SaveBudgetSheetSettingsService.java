package com.ohgiraffer.approval.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.command.SaveBudgetSheetSettingsCommand;
import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.port.BudgetSheetRow;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.BudgetAllocationRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaveBudgetSheetSettingsService
        implements SaveBudgetSheetSettingsUseCase {

    private final BudgetSheetPort budgetSheetPort;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SaveBudgetSheetSettingsService(
            BudgetSheetPort budgetSheetPort,
            BudgetCategoryRepository budgetCategoryRepository,
            BudgetAllocationRepository budgetAllocationRepository,
            ExternalSheetLinkRepository externalSheetLinkRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.budgetSheetPort = budgetSheetPort;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.budgetAllocationRepository = budgetAllocationRepository;
        this.externalSheetLinkRepository = externalSheetLinkRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BudgetSyncResult saveAndSync(
            SaveBudgetSheetSettingsCommand command
    ) {
        validateCommand(
                command
        );

        String spreadsheetId =
                budgetSheetPort.extractSpreadsheetId(
                        command.spreadsheetUrl()
                );

        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        List<BudgetSheetRow> rows =
                budgetSheetPort.readBudgetRows(
                        spreadsheetId,
                        command.sheetName(),
                        command.columnMapping()
                );

        if (rows.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "동기화할 예산 데이터가 없습니다."
            );
        }

        saveSheetSettings(
                command,
                now
        );

        int syncedCount = 0;

        for (BudgetSheetRow row : rows) {
            BudgetCategory category =
                    saveOrUpdateCategory(
                            row.categoryName()
                    );

            saveOrUpdateAllocation(
                    category.getId(),
                    row,
                    now
            );

            syncedCount++;
        }

        return new BudgetSyncResult(
                syncedCount,
                now
        );
    }

    private void validateCommand(
            SaveBudgetSheetSettingsCommand command
    ) {
        if (command.spreadsheetUrl() == null
                || command.spreadsheetUrl().isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        if (command.sheetName() == null
                || command.sheetName().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 시트명은 필수입니다."
            );
        }

        validateColumnMapping(
                command.columnMapping()
        );
    }

    private void validateColumnMapping(
            BudgetColumnMapping columnMapping
    ) {
        if (columnMapping == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 컬럼 매핑 정보는 필수입니다."
            );
        }

        if (isBlank(columnMapping.category())
                || isBlank(columnMapping.totalAmount())
                || isBlank(columnMapping.usedAmount())
                || isBlank(columnMapping.remainingAmount())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "카테고리, 예산액, 사용액, 잔여액 컬럼 매핑은 모두 필수입니다."
            );
        }
    }

    private void saveSheetSettings(
            SaveBudgetSheetSettingsCommand command,
            LocalDateTime syncedAt
    ) {
        String columnMappingJson =
                toJson(
                        command.columnMapping()
                );

        ExternalSheetLink externalSheetLink =
                externalSheetLinkRepository
                        .findByDomain(
                                ExternalSheetLink.budgetDomain()
                        )
                        .map(existingLink -> {
                            existingLink.update(
                                    command.spreadsheetUrl(),
                                    command.sheetName(),
                                    columnMappingJson,
                                    syncedAt
                            );

                            return existingLink;
                        })
                        .orElseGet(() ->
                                ExternalSheetLink.createBudgetLink(
                                        command.spreadsheetUrl(),
                                        command.sheetName(),
                                        columnMappingJson,
                                        syncedAt
                                )
                        );

        externalSheetLinkRepository.save(
                externalSheetLink
        );
    }

    private BudgetCategory saveOrUpdateCategory(
            String categoryName
    ) {
        validateCategoryName(
                categoryName
        );

        return budgetCategoryRepository
                .findByName(
                        categoryName
                )
                .orElseGet(() ->
                        budgetCategoryRepository.save(
                                BudgetCategory.createFromSheet(
                                        categoryName
                                )
                        )
                );
    }

    private void saveOrUpdateAllocation(
            Long budgetCategoryId,
            BudgetSheetRow row,
            LocalDateTime syncedAt
    ) {
        validateAmounts(
                row
        );

        BudgetAllocation budgetAllocation =
                budgetAllocationRepository
                        .findByBudgetCategoryId(
                                budgetCategoryId
                        )
                        .map(existingAllocation -> {
                            existingAllocation.updateAmounts(
                                    row.totalAmount(),
                                    row.usedAmount(),
                                    row.remainingAmount(),
                                    syncedAt
                            );

                            return existingAllocation;
                        })
                        .orElseGet(() ->
                                BudgetAllocation.create(
                                        budgetCategoryId,
                                        row.totalAmount(),
                                        row.usedAmount(),
                                        row.remainingAmount(),
                                        syncedAt
                                )
                        );

        budgetAllocationRepository.save(
                budgetAllocation
        );
    }

    private void validateCategoryName(
            String categoryName
    ) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 카테고리명은 필수입니다."
            );
        }
    }

    private void validateAmounts(
            BudgetSheetRow row
    ) {
        if (row.totalAmount() == null
                || row.usedAmount() == null
                || row.remainingAmount() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산액, 사용액, 잔여액은 필수입니다."
            );
        }

        if (row.totalAmount().signum() < 0
                || row.usedAmount().signum() < 0
                || row.remainingAmount().signum() < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산액, 사용액, 잔여액은 음수일 수 없습니다."
            );
        }
    }

    private String toJson(
            BudgetColumnMapping columnMapping
    ) {
        try {
            return objectMapper.writeValueAsString(
                    columnMapping
            );

        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "예산 컬럼 매핑 정보를 저장할 수 없습니다."
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}