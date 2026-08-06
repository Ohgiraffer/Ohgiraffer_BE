package com.ohgiraffer.user.presentation.api.request;

import com.ohgiraffer.user.domain.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusChangeRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        UserStatus status
) {
}
