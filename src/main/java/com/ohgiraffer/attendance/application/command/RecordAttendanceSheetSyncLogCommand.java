package com.ohgiraffer.attendance.application.command;


import com.ohgiraffer.attendance.domain.model.SyncResult;

public record RecordAttendanceSheetSyncLogCommand(
        Long attendanceSheetLinkId,
        String changedRange,
        String diffSummary,
        Long executorId,
        String executorName,
        SyncResult result
) {
}