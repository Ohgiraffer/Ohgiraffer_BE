package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.usecase.BudgetSheetValidationResult;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.application.usecase.ValidateBudgetSheetUseCase;
import com.ohgiraffer.approval.presentation.api.request.SaveBudgetSheetSettingsRequest;
import com.ohgiraffer.approval.presentation.api.request.ValidateBudgetSheetRequest;
import com.ohgiraffer.approval.presentation.api.response.BudgetSheetValidationResponse;
import com.ohgiraffer.approval.presentation.api.response.BudgetSyncResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budgets/sheets")
public class BudgetController {

    private final ValidateBudgetSheetUseCase validateBudgetSheetUseCase;
    private final SaveBudgetSheetSettingsUseCase saveBudgetSheetSettingsUseCase;

    public BudgetController(
            ValidateBudgetSheetUseCase validateBudgetSheetUseCase,
            SaveBudgetSheetSettingsUseCase saveBudgetSheetSettingsUseCase
    ) {
        this.validateBudgetSheetUseCase = validateBudgetSheetUseCase;
        this.saveBudgetSheetSettingsUseCase = saveBudgetSheetSettingsUseCase;
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BudgetSheetValidationResponse> validateBudgetSheet(
            @Valid @RequestBody ValidateBudgetSheetRequest request
    ) {
        BudgetSheetValidationResult result =
                validateBudgetSheetUseCase.validate(
                        request.spreadsheetUrl()
                );

        return ResponseEntity.ok(
                BudgetSheetValidationResponse.from(
                        result
                )
        );
    }

    @PostMapping("/settings")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BudgetSyncResponse> saveBudgetSheetSettings(
            @Valid @RequestBody SaveBudgetSheetSettingsRequest request
    ) {
        BudgetSyncResult result =
                saveBudgetSheetSettingsUseCase.saveAndSync(
                        request.toCommand()
                );

        return ResponseEntity.ok(
                BudgetSyncResponse.from(
                        result
                )
        );
    }
}