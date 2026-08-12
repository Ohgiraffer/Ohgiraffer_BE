package com.ohgiraffer.attendance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttendanceSheetSyncLog {

    private final Long syncLogId;
    private final Long attendanceSheetLinkId;
    private final String changedRange;
    private final String diffSummary;
    private final LocalDateTime syncedAt;
    private final Long executorId;
    private final String executorName;
    private final SyncResult result;

    @Builder
    private AttendanceSheetSyncLog(
            Long syncLogId,
            Long attendanceSheetLinkId,
            String changedRange,
            String diffSummary,
            LocalDateTime syncedAt,
            Long executorId,
            String executorName,
            SyncResult result
    ) {
        this.syncLogId = syncLogId;
        this.attendanceSheetLinkId = attendanceSheetLinkId;
        this.changedRange = changedRange;
        this.diffSummary = diffSummary;
        this.syncedAt = syncedAt;
        this.executorId = executorId;
        this.executorName = executorName;
        this.result = result;
    }
}