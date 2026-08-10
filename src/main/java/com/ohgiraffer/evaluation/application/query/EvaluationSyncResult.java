package com.ohgiraffer.evaluation.application.query;

import java.util.List;

/**
 * 동기화 한 번의 결과.
 *
 * <p>{@code skipped} 는 반영하지 못한 행이다. 시트가 100행인데 이메일 오타 하나로
 * 전부 막히면 안 되므로, 그 행만 건너뛰고 이유를 담아 돌려준다.
 */
public record EvaluationSyncResult(
        long sheetLinkId,
        int addedCount,
        int updatedCount,
        List<SkippedRow> skipped
) {

    public int changedCount() {
        return addedCount + updatedCount;
    }

    /**
     * @param rowNumber 시트에서 보이는 줄 번호. 헤더가 1행이다
     */
    public record SkippedRow(int rowNumber, String reason) {
    }
}
