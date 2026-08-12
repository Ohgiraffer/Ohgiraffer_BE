package com.ohgiraffer.attendance.domain.dto;

import java.util.List;

public record SyncAttendanceSheetResult(
        int totalCount,
        int successCount,
        int failedCount,
        List<FailedRow> failedRows
) {
    public record FailedRow(int rowIndex, String userId, String reason) {
    }
}