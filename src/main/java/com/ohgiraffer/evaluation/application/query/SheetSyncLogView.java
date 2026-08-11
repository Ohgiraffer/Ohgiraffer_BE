package com.ohgiraffer.evaluation.application.query;

import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;

import java.time.Instant;

/**
 * 이력 화면용 조회 모델.
 *
 * <p>실행자 이름은 사용자 도메인에서 가져오므로 이력 자체에는 없다. 여기서 합쳐 담는다.
 * 찾지 못하면 null 이며, 화면이 "시스템" 이나 "알 수 없음" 으로 표시하면 된다.
 */
public record SheetSyncLogView(
        Long syncLogId,
        Long executedBy,
        String executedByName,
        int changedCount,
        String diffSummary,
        Instant syncedAt
) {

    public static SheetSyncLogView of(SheetSyncLog syncLog, String executedByName) {
        return new SheetSyncLogView(
                syncLog.getId(),
                syncLog.getExecutedBy(),
                executedByName,
                syncLog.getChangedCount(),
                syncLog.getDiffSummary(),
                syncLog.getSyncedAt()
        );
    }
}
