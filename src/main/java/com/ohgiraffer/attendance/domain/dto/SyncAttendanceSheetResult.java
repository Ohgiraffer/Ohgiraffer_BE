package com.ohgiraffer.attendance.domain.dto;

import com.ohgiraffer.attendance.domain.model.FailedRowDetail;

import java.util.List;

public record SyncAttendanceSheetResult(
        int totalCount,
        int successCount,
        int failedCount,
        List<FailedRowDetail> failedRows
) {
    public static SyncAttendanceSheetResult of(
            int totalCount, int successCount, List<FailedRowDetail> failedRows) {
        return new SyncAttendanceSheetResult(
                totalCount,
                successCount,
                failedRows.size(),
                failedRows
        );
    }
}