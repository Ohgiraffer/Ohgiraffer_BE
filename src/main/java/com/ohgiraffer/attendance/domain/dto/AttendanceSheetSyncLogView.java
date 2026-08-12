package com.ohgiraffer.attendance.domain.dto;

import com.ohgiraffer.attendance.domain.model.AttendanceSheetSyncLog;
import com.ohgiraffer.attendance.domain.model.FailedRowDetail;
import com.ohgiraffer.attendance.domain.model.SyncResult;

import java.time.LocalDateTime;
import java.util.List;

public record AttendanceSheetSyncLogView(
        Long syncLogId,
        LocalDateTime syncedAt,
        String executorName,
        int successCount,
        List<FailedRowDetail> failedRows,
        SyncResult result
) {
    public static AttendanceSheetSyncLogView from(AttendanceSheetSyncLog log) {
        int successCount = 0;
        try {
            successCount = Integer.parseInt(log.getDiffSummary());
        } catch (NumberFormatException ignored) {
        }

        return new AttendanceSheetSyncLogView(
                log.getSyncLogId(),
                log.getSyncedAt(),
                log.getExecutorName(),
                successCount,
                log.getFailedRowDetails() != null ? log.getFailedRowDetails() : List.of(),
                log.getResult()
        );
    }
}