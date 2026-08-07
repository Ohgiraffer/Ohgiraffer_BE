package com.ohgiraffer.global.google.sheets;

public interface ValidateExternalSheetUseCase {

    ExternalSheetValidationResult validate(
            String spreadsheetUrl
    );
}