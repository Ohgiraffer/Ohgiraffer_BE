package com.ohgiraffer.approval.presentation.api.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateLeaveApprovalRequest(
        @NotNull(message = "결재자 아이디는 필수입니다.")
        Long approverId,

        @NotNull(message = "휴가 시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "휴가 종료일은 필수입니다.")
        LocalDate endDate
) {
}