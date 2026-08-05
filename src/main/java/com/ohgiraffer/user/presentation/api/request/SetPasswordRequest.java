package com.ohgiraffer.user.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetPasswordRequest(

        @Schema(description = "변경할 비밀번호", example = "NewPass1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,16}$",
                message = "비밀번호는 영문과 특수기호를 포함해 8~16자로 입력해주세요."
        )
        String newPassword
) {
}
