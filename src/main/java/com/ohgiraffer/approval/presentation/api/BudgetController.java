package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.query.BudgetSummaryResult;
import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;
import com.ohgiraffer.approval.application.usecase.GetBudgetSummaryUseCase;
import com.ohgiraffer.approval.application.usecase.SaveBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.application.usecase.SyncBudgetSheetUseCase;
import com.ohgiraffer.approval.presentation.api.request.SaveBudgetSheetSettingsRequest;
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

    public BudgetController(
            SaveBudgetSheetSettingsUseCase saveBudgetSheetSettingsUseCase,
            SyncBudgetSheetUseCase syncBudgetSheetUseCase,
            GetBudgetSummaryUseCase getBudgetSummaryUseCase
    ) {
        this.saveBudgetSheetSettingsUseCase = saveBudgetSheetSettingsUseCase;
        this.syncBudgetSheetUseCase = syncBudgetSheetUseCase;
        this.getBudgetSummaryUseCase = getBudgetSummaryUseCase;
    }

    @PostMapping("/sheets/settings")
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

    @PostMapping("/sync")
    @PreAuthorize("hasRole('MANAGER')")
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
    @PreAuthorize("hasRole('MANAGER')")
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