package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.command.SaveBudgetSheetSettingsCommand;

public interface SaveBudgetSheetSettingsUseCase {

    BudgetSyncResult saveAndSync(
            SaveBudgetSheetSettingsCommand command
    );
}