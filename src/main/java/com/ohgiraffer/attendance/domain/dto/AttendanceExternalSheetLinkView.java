package com.ohgiraffer.attendance.domain.dto;

import com.ohgiraffer.attendance.domain.model.AttendanceExternalSheetLink;

import java.time.LocalDateTime;
import java.util.Map;

public record AttendanceExternalSheetLinkView(
        Long attendanceSheetLinkId,
        String sheetUrl,
        String tabName,
        Map<String, String> columnMapping,
        LocalDateTime lastSyncedAt
) {
    public static AttendanceExternalSheetLinkView from(AttendanceExternalSheetLink link) {
        return new AttendanceExternalSheetLinkView(
                link.getAttendanceSheetLinkId(),
                link.getSheetUrl(),
                link.getTabName(),
                link.getColumnMapping(),
                link.getLastSyncedAt()
        );
    }
}