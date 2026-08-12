package com.ohgiraffer.attendance.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "attendance_external_sheet_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceExternalSheetLinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_sheet_link_id")
    private Long attendanceSheetLinkId;

    @Column(name = "sheet_url", nullable = false, length = 500)
    private String sheetUrl;

    @Column(name = "tab_name", nullable = false, length = 100)
    private String tabName;

    @Column(name = "date_cell_range", nullable = false, length = 200)
    private String dateCellRange;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "column_mapping", nullable = false)
    private Map<String, String> columnMapping;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private AttendanceExternalSheetLinkJpaEntity(
            Long attendanceSheetLinkId,
            String sheetUrl,
            String tabName,
            String dateCellRange,
            Map<String, String> columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        this.attendanceSheetLinkId = attendanceSheetLinkId;
        this.sheetUrl = sheetUrl;
        this.tabName = tabName;
        this.dateCellRange = dateCellRange;
        this.columnMapping = columnMapping;
        this.lastSyncedAt = lastSyncedAt;
    }
}