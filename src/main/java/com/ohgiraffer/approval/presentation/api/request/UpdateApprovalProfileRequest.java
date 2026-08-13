package com.ohgiraffer.approval.presentation.api.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateApprovalProfileRequest(

        @NotNull
        LocalDate birthDate
) {
}