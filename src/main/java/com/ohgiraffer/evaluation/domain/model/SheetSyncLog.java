package com.ohgiraffer.evaluation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;
import java.util.List;

/**
 * 동기화 한 번의 기록. JPA와 무관한 순수 객체다.
 *
 * <p>변경이 없으면 남기지 않는다. 화면의 이력 목록은 "언제 무엇이 몇 건 바뀌었나" 를 보는
 * 곳이라, 눌렀지만 바뀐 것이 없는 실행까지 쌓이면 정작 볼 것이 묻힌다.
 *
 * <p>{@code executedBy} 가 비어 있을 수 있다. 지금은 사람이 버튼을 눌러야만 실행되지만,
 * 나중에 주기 실행을 붙이면 누른 사람이 없다. 그때는 화면이 "시스템" 으로 표시하면 된다.
 */
public class SheetSyncLog {

    private final Long id;
    private final Long sheetLinkId;
    private final Long executedBy;
    private final int changedCount;
    private final List<TraineeChangeSummary> summaries;
    private final Instant syncedAt;

    private SheetSyncLog(
            Long id,
            Long sheetLinkId,
            Long executedBy,
            int changedCount,
            List<TraineeChangeSummary> summaries,
            Instant syncedAt
    ) {
        this.id = id;
        this.sheetLinkId = sheetLinkId;
        this.executedBy = executedBy;
        this.changedCount = changedCount;
        this.summaries = summaries == null ? List.of() : List.copyOf(summaries);
        this.syncedAt = syncedAt;
    }

    public static SheetSyncLog create(
            Long sheetLinkId,
            Long executedBy,
            int changedCount,
            List<TraineeChangeSummary> summaries
    ) {
        if (sheetLinkId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트 연동 정보가 필요합니다."
            );
        }

        /*
         * 변경이 없으면 이력을 만들지 않는다는 규칙을 여기서 지킨다.
         * 호출하는 쪽에서만 걸러 두면 호출자가 늘어날 때 규칙이 조용히 깨진다.
         */
        if (changedCount <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "변경이 없으면 동기화 이력을 남기지 않습니다."
            );
        }

        return new SheetSyncLog(
                null,
                sheetLinkId,
                executedBy,
                changedCount,
                summaries,
                Instant.now()
        );
    }

    /**
     * 저장소에서 읽어온 값으로 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static SheetSyncLog restore(
            Long id,
            Long sheetLinkId,
            Long executedBy,
            int changedCount,
            List<TraineeChangeSummary> summaries,
            Instant syncedAt
    ) {
        return new SheetSyncLog(
                id, sheetLinkId, executedBy, changedCount, summaries, syncedAt
        );
    }

    public List<TraineeChangeSummary> getSummaries() {
        return summaries;
    }

    /**
     * 알림 본문처럼 카드를 그릴 수 없는 곳에서 쓸 한 덩어리 글.
     */
    public String toSummaryText() {
        return EvaluationDiffSummaryWriter.toText(summaries);
    }

    public Long getId() {
        return id;
    }

    public Long getSheetLinkId() {
        return sheetLinkId;
    }

    public Long getExecutedBy() {
        return executedBy;
    }

    public int getChangedCount() {
        return changedCount;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
