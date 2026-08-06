package com.ohgiraffer.user.presentation.api.response;

import java.util.List;

public record UserSheetConnectionResponse(
        String spreadsheetTitle,
        List<String> sheetNames,
        String selectedSheetName,
        List<String> columns,
        int totalCount,
        int validCount,
        int invalidCount,
        List<UserSheetRowResponse> rows
) {}