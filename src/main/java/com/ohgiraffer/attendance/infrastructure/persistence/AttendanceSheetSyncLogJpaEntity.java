package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.SyncResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sheet_sync_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSheetSyncLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_log_id")
    private Long syncLogId;

    @Column(name = "attendance_sheet_link_id")
    private Long attendanceSheetLinkId;

    @Column(name = "changed_range", length = 100)
    private String changedRange;

    @Column(name = "diff_summary", columnDefinition = "TEXT")
    private String diffSummary;

    @CreationTimestamp
    @Column(name = "synced_at", nullable = false, updatable = false)
    private LocalDateTime syncedAt;

    @Column(name = "executor_id")
    private Long executorId;

    @Column(name = "executor_name", length = 100)
    private String executorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private SyncResult result;

    @Builder
    private AttendanceSheetSyncLogJpaEntity(
            Long attendanceSheetLinkId,
            String changedRange,
            String diffSummary,
            Long executorId,
            String executorName,
            SyncResult result
    ) {
        this.attendanceSheetLinkId = attendanceSheetLinkId;
        this.changedRange = changedRange;
        this.diffSummary = diffSummary;
        this.executorId = executorId;
        this.executorName = executorName;
        this.result = result;
    }
}