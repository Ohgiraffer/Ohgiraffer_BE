package com.ohgiraffer.user.presentation.api.response;

import java.util.List;

public record UserSheetConnectionResponse(
        String filename,
        List<UserSheetRowResponse> rows
) {}