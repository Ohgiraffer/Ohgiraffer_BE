package com.ohgiraffer.attendance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class AttendanceExternalSheetLink {

    private final Long attendanceSheetLinkId;
    private final String sheetUrl;
    private final String tabName;
    private final String dateCellRange;
    private final Map<String, String> columnMapping;
    private final LocalDateTime lastSyncedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    private AttendanceExternalSheetLink(
            Long attendanceSheetLinkId,
            String sheetUrl,
            String tabName,
            String dateCellRange,
            Map<String, String> columnMapping,
            LocalDateTime lastSyncedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.attendanceSheetLinkId = attendanceSheetLinkId;
        this.sheetUrl = sheetUrl;
        this.tabName = tabName;
        this.dateCellRange = dateCellRange;
        this.columnMapping = columnMapping;
        this.lastSyncedAt = lastSyncedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AttendanceExternalSheetLink withSyncedNow(LocalDateTime syncedAt) {
        return AttendanceExternalSheetLink.builder()
                .attendanceSheetLinkId(this.attendanceSheetLinkId)
                .sheetUrl(this.sheetUrl)
                .tabName(this.tabName)
                .dateCellRange(this.dateCellRange)
                .columnMapping(this.columnMapping)
                .lastSyncedAt(syncedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}