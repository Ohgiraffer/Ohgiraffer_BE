package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.SaveBudgetSheetSettingsCommand;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveBudgetSheetSettingsService implements SaveBudgetSheetSettingsUseCase {

    private final BudgetSheetSyncService budgetSheetSyncService;

    @Override
    public BudgetSyncResult saveAndSync(
            SaveBudgetSheetSettingsCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        return budgetSheetSyncService.sync(
                command.spreadsheetUrl(),
                command.sheetName(),
                command.columnMapping()
        );
    }
}