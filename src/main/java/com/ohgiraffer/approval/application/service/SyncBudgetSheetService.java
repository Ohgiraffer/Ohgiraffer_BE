package com.ohgiraffer.approval.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.SyncBudgetSheetUseCase;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncBudgetSheetService implements SyncBudgetSheetUseCase {

    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final BudgetSheetSyncService budgetSheetSyncService;
    private final ObjectMapper objectMapper;

    @Override
    public BudgetSyncResult sync() {
        ExternalSheetLink externalSheetLink = externalSheetLinkRepository.findByDomain(
                        ExternalSheetLink.budgetDomain()
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE
                ));

        BudgetColumnMapping columnMapping = toColumnMapping(
                externalSheetLink.getColumnMapping()
        );

        return budgetSheetSyncService.sync(
                externalSheetLink.getSheetUrl(),
                externalSheetLink.getTabName(),
                columnMapping
        );
    }

    private BudgetColumnMapping toColumnMapping(
            String columnMappingJson
    ) {
        try {
            return objectMapper.readValue(
                    columnMappingJson,
                    BudgetColumnMapping.class
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    exception
            );
        }
    }
}