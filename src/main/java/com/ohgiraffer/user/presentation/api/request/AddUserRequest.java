package com.ohgiraffer.user.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddUserRequest(

        @NotEmpty(message = "등록할 사용자가 최소 1명 필요합니다.")
        @Valid
        List<UserSheetConfirmRow> rows
) {
    public record UserSheetConfirmRow(
            String name,
            String email,
            String phone,
            String role
    ) {}
}