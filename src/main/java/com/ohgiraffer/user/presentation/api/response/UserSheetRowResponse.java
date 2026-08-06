package com.ohgiraffer.user.presentation.api.response;

import java.util.List;

public record UserSheetRowResponse(
        int rowNumber,
        String name,
        String email,
        String phone,
        String rawRole,
        boolean valid,
        List<String> errors
) {}