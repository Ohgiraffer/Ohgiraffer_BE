package com.ohgiraffer.approval.application.query;

import java.time.LocalDate;

public record ApprovalProfileResult(
        LocalDate birthDate,
        String phoneNumber
) {
}