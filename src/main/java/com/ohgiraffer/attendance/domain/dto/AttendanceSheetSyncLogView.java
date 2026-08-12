package com.ohgiraffer.attendance.domain.dto;

import com.ohgiraffer.attendance.domain.model.AttendanceSheetSyncLog;
import com.ohgiraffer.attendance.domain.model.SyncResult;

import java.time.LocalDateTime;

public record AttendanceSheetSyncLogView(
        Long syncLogId,
        Long attendanceSheetLinkId,
        String changedRange,
        String diffSummary,
        LocalDateTime syncedAt,
        Long executorId,
        String executorName,
        SyncResult result
) {
    public static AttendanceSheetSyncLogView from(AttendanceSheetSyncLog log) {
        return new AttendanceSheetSyncLogView(
                log.getSyncLogId(),
                log.getAttendanceSheetLinkId(),
                log.getChangedRange(),
                log.getDiffSummary(),
                log.getSyncedAt(),
                log.getExecutorId(),
                log.getExecutorName(),
                log.getResult()
        );
    }
}