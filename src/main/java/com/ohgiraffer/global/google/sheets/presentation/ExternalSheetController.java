package com.ohgiraffer.global.google.sheets.presentation;

import com.ohgiraffer.global.google.sheets.ExternalSheetValidationResult;
import com.ohgiraffer.global.google.sheets.ValidateExternalSheetUseCase;
import com.ohgiraffer.global.google.sheets.presentation.request.ValidateExternalSheetRequest;
import com.ohgiraffer.global.google.sheets.presentation.response.ExternalSheetValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-sheets")
public class ExternalSheetController {

    private final ValidateExternalSheetUseCase validateExternalSheetUseCase;

    public ExternalSheetController(
            ValidateExternalSheetUseCase validateExternalSheetUseCase
    ) {
        this.validateExternalSheetUseCase = validateExternalSheetUseCase;
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<ExternalSheetValidationResponse> validateExternalSheet(
            @Valid @RequestBody ValidateExternalSheetRequest request
    ) {
        ExternalSheetValidationResult result =
                validateExternalSheetUseCase.validate(
                        request.spreadsheetUrl()
                );

        return ResponseEntity.ok(
                ExternalSheetValidationResponse.from(
                        result
                )
        );
    }
}