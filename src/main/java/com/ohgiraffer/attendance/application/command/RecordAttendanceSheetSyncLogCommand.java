package com.ohgiraffer.attendance.application.command;

import com.ohgiraffer.attendance.domain.model.FailedRowDetail;
import com.ohgiraffer.attendance.domain.model.SyncResult;

import java.util.List;

public record RecordAttendanceSheetSyncLogCommand(
        Long attendanceSheetLinkId,
        String changedRange,
        String diffSummary,
        List<FailedRowDetail> failedRowDetails,
        Long executorId,
        String executorName,
        SyncResult result
) {
}