package com.ohgiraffer.approval.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.command.SaveBudgetSheetSettingsCommand;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveBudgetSheetSettingsService implements SaveBudgetSheetSettingsUseCase {

    private static final int REQUIRED_MAPPING_COUNT = 4;

    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final BudgetSheetSyncService budgetSheetSyncService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public BudgetSyncResult saveAndSync(
            SaveBudgetSheetSettingsCommand command
    ) {
        validateCommand(
                command
        );

        LocalDateTime now = LocalDateTime.now(
                clock
        );

        BudgetColumnMapping normalizedColumnMapping = normalizeColumnMapping(
                command.columnMapping()
        );

        saveOrUpdateExternalSheetLink(
                command.spreadsheetUrl().strip(),
                command.sheetName().strip(),
                normalizedColumnMapping,
                now
        );

        return budgetSheetSyncService.sync(
                command.spreadsheetUrl().strip(),
                command.sheetName().strip(),
                normalizedColumnMapping
        );
    }

    private void validateCommand(
            SaveBudgetSheetSettingsCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (isBlank(
                command.spreadsheetUrl()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (isBlank(
                command.sheetName()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
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

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}