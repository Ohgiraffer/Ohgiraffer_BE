package com.ohgiraffer.approval.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectApprovalRequest(
        @NotBlank(message = "반려 사유는 필수입니다.")
        @Size(max = 500, message = "반려 사유는 500자 이하로 입력해주세요.")
        String rejectionReason
) {
}