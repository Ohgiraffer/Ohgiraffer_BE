package com.ohgiraffer.attendance.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record SaveAttendanceSheetLinkRequest(
        @NotBlank String sheetUrl,
        @NotBlank String tabName,
        @NotBlank String dateCellRange,
        @NotEmpty Map<String, String> columnMapping
) {
}