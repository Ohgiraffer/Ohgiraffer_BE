package com.ohgiraffer.approval.presentation.api.request;

import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.command.SaveBudgetSheetSettingsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveBudgetSheetSettingsRequest(
        @NotBlank(message = "스프레드시트 URL은 필수입니다.")
        String spreadsheetUrl,

        @NotBlank(message = "예산 시트명은 필수입니다.")
        String sheetName,

        @Valid
        @NotNull(message = "예산 컬럼 매핑 정보는 필수입니다.")
        BudgetColumnMappingRequest columnMapping
) {

    public SaveBudgetSheetSettingsCommand toCommand() {
        return new SaveBudgetSheetSettingsCommand(
                spreadsheetUrl,
                sheetName,
                new BudgetColumnMapping(
                        columnMapping.category(),
                        columnMapping.totalAmount(),
                        columnMapping.usedAmount(),
                        columnMapping.remainingAmount()
                )
        );
    }

    public record BudgetColumnMappingRequest(
            @NotBlank(message = "카테고리 컬럼은 필수입니다.")
            String category,

            @NotBlank(message = "예산액 컬럼은 필수입니다.")
            String totalAmount,

            @NotBlank(message = "사용액 컬럼은 필수입니다.")
            String usedAmount,

            @NotBlank(message = "잔여액 컬럼은 필수입니다.")
            String remainingAmount
    ) {
    }
}