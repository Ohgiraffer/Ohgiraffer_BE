package com.ohgiraffer.approval.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateBudgetSheetRequest(
        @NotBlank(message = "스프레드시트 URL은 필수입니다.")
        String spreadsheetUrl
) {
}