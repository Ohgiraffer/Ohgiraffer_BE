package com.ohgiraffer.global.google.sheets.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateExternalSheetRequest(
        @NotBlank(message = "스프레드시트 URL은 필수입니다.")
        String spreadsheetUrl
) {
}