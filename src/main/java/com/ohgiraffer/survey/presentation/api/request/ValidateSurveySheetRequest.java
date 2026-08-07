package com.ohgiraffer.survey.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidateSurveySheetRequest(

        @NotBlank(
                message = "Google Spreadsheet URL은 필수입니다."
        )
        @Size(
                max = 500,
                message = "Google Spreadsheet URL은 500자 이하여야 합니다."
        )
        String spreadsheetUrl,

        @Size(
                max = 255,
                message = "Google Sheet 이름은 255자 이하여야 합니다."
        )
        String sheetName
) {
}