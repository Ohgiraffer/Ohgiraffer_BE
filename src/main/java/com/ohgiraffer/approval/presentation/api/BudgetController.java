package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.query.BudgetSheetSettingsResult;
import com.ohgiraffer.approval.application.query.BudgetSummaryResult;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.GetBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.application.usecase.GetBudgetSummaryUseCase;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.application.usecase.SyncBudgetSheetUseCase;
import com.ohgiraffer.approval.presentation.api.request.SaveBudgetSheetSettingsRequest;
import com.ohgiraffer.approval.presentation.api.response.BudgetSheetSettingsResponse;
import com.ohgiraffer.approval.presentation.api.response.BudgetSummaryResponse;
import com.ohgiraffer.approval.presentation.api.response.BudgetSyncResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final SaveBudgetSheetSettingsUseCase saveBudgetSheetSettingsUseCase;
    private final SyncBudgetSheetUseCase syncBudgetSheetUseCase;
    private final GetBudgetSummaryUseCase getBudgetSummaryUseCase;
    private final GetBudgetSheetSettingsUseCase getBudgetSheetSettingsUseCase;

    public BudgetController(
            SaveBudgetSheetSettingsUseCase saveBudgetSheetSettingsUseCase,
            SyncBudgetSheetUseCase syncBudgetSheetUseCase,
            GetBudgetSummaryUseCase getBudgetSummaryUseCase,
            GetBudgetSheetSettingsUseCase getBudgetSheetSettingsUseCase
    ) {
        this.saveBudgetSheetSettingsUseCase = saveBudgetSheetSettingsUseCase;
        this.syncBudgetSheetUseCase = syncBudgetSheetUseCase;
        this.getBudgetSummaryUseCase = getBudgetSummaryUseCase;
        this.getBudgetSheetSettingsUseCase = getBudgetSheetSettingsUseCase;
    }

    @PostMapping("/sheets/settings")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
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

    @GetMapping("/sheets/settings")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<BudgetSheetSettingsResponse> getBudgetSheetSettings() {
        BudgetSheetSettingsResult result =
                getBudgetSheetSettingsUseCase.getSettings();

        return ResponseEntity.ok(
                BudgetSheetSettingsResponse.from(
                        result
                )
        );
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<BudgetSyncResponse> syncBudgetSheet() {
        BudgetSyncResult result =
                syncBudgetSheetUseCase.sync();

        return ResponseEntity.ok(
                BudgetSyncResponse.from(
                        result
                )
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<BudgetSummaryResponse> getBudgetSummary() {
        BudgetSummaryResult result =
                getBudgetSummaryUseCase.getSummary();

        return ResponseEntity.ok(
                BudgetSummaryResponse.from(
                        result
                )
        );
    }
}