package com.ohgiraffer.attendance.domain.model;

public record FailedRowDetail(
        int rowNumber,
        String reason
) {
}