package com.ohgiraffer.survey.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveSurveySheetLinkRequest(

        @NotBlank(
                message = "Google Spreadsheet URL은 필수입니다."
        )
        @Size(
                max = 500,
                message = "Google Spreadsheet URL은 500자 이하여야 합니다."
        )
        String spreadsheetUrl,

        @NotBlank(
                message = "Google Sheet 이름은 필수입니다."
        )
        @Size(
                max = 255,
                message = "Google Sheet 이름은 255자 이하여야 합니다."
        )
        String sheetName,

        @NotBlank(
                message = "응답자 식별 컬럼은 필수입니다."
        )
        @Size(
                max = 255,
                message = "응답자 식별 컬럼은 255자 이하여야 합니다."
        )
        String respondentColumn,

        @NotBlank(
                message = "응답 일시 컬럼은 필수입니다."
        )
        @Size(
                max = 255,
                message = "응답 일시 컬럼은 255자 이하여야 합니다."
        )
        String submittedAtColumn
) {
}