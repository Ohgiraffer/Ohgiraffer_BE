package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 시트 동기화 이력.
 *
 * <p>{@code changed_range} 컬럼은 매핑하지 않는다. baseline 에 있으나 우리는 탭 전체를
 * 한 번에 읽어 어느 범위가 바뀌었는지 따로 표시할 것이 없다. ERD 정리 대상이다.
 */
@Entity
@Table(name = "sheet_sync_log")
public class SheetSyncLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_log_id")
    private Long id;

    @Column(name = "sheet_link_id", nullable = false)
    private Long sheetLinkId;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "changed_count", nullable = false)
    private int changedCount;

    @Column(name = "diff_summary", columnDefinition = "TEXT")
    private String diffSummary;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected SheetSyncLogJpaEntity() {
    }

    private SheetSyncLogJpaEntity(SheetSyncLog syncLog) {
        this.id = syncLog.getId();
        this.sheetLinkId = syncLog.getSheetLinkId();
        this.executedBy = syncLog.getExecutedBy();
        this.changedCount = syncLog.getChangedCount();
        this.diffSummary = syncLog.getDiffSummary();
        this.syncedAt = syncLog.getSyncedAt();
    }

    public static SheetSyncLogJpaEntity from(SheetSyncLog syncLog) {
        return new SheetSyncLogJpaEntity(syncLog);
    }

    public SheetSyncLog toDomain() {
        return SheetSyncLog.restore(
                id, sheetLinkId, executedBy, changedCount, diffSummary, syncedAt
        );
    }
}
