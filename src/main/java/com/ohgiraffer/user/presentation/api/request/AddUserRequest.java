package com.ohgiraffer.user.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddUserRequest(

        @NotEmpty(message = "등록할 사용자가 최소 1명 필요합니다.")
        @Valid
        List<UserSheetConfirmRow> rows
) {
    public record UserSheetConfirmRow(
            @NotBlank(message = "이름은 필수입니다.")
            String name,

            @NotBlank(message = "이메일은 필수입니다.")
            String email,

            @NotBlank(message = "전화번호는 필수입니다.")
            String phone,

            @NotBlank(message = "역할은 필수입니다.")
            String role
    ) {}
}