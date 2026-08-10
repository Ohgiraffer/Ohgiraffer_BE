package com.ohgiraffer.evaluation.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 변경 목록을 사람이 읽을 수 있는 글로 바꾼다.
 *
 * <p>이력에 남는 요약문을 만든다. 나중에 이 자리를 AI 요약으로 바꿀 예정이라,
 * 그때 갈아끼울 수 있도록 요약을 만드는 일만 따로 떼어 두었다.
 *
 * <p>단순히 바뀐 값을 나열하지 않고 점수 변화를 먼저 적는다. 이력을 보는 사람이
 * 가장 먼저 확인하는 것이 점수이기 때문이다.
 */
public final class EvaluationDiffSummaryWriter {

    private EvaluationDiffSummaryWriter() {
    }

    public static String write(List<EvaluationChange> changes) {
        if (changes.isEmpty()) {
            return "변경된 평가가 없습니다.";
        }

        StringBuilder summary = new StringBuilder();

        for (EvaluationChange change : changes) {
            if (!summary.isEmpty()) {
                summary.append(System.lineSeparator());
            }

            summary.append(writeOne(change));
        }

        return summary.toString();
    }

    private static String writeOne(EvaluationChange change) {
        StringBuilder line = new StringBuilder()
                .append(change.type() == EvaluationChange.Type.ADDED ? "[추가] " : "[수정] ")
                .append(change.traineeName())
                .append(" · ")
                .append(change.evaluationType())
                .append(" · ")
                .append(change.item());

        if (change.type() == EvaluationChange.Type.ADDED) {
            line.append(" — 점수 ").append(format(change.newScore()));

            if (change.newComment() != null) {
                line.append(", 의견 \"").append(change.newComment()).append("\"");
            }

            return line.toString();
        }

        if (change.scoreChanged()) {
            line.append(" — 점수 ")
                    .append(format(change.previousScore()))
                    .append(" → ")
                    .append(format(change.newScore()));
        }

        if (change.commentChanged()) {
            line.append(change.scoreChanged() ? ", 의견 " : " — 의견 ")
                    .append(quote(change.previousComment()))
                    .append(" → ")
                    .append(quote(change.newComment()));
        }

        return line.toString();
    }

    /**
     * 점수는 보이는 대로 적는다. 85.00 을 그대로 두면 사람이 읽기 번거롭다.
     */
    private static String format(BigDecimal score) {
        if (score == null) {
            return "없음";
        }

        return score.stripTrailingZeros().toPlainString();
    }

    private static String quote(String comment) {
        if (comment == null || comment.isBlank()) {
            return "없음";
        }

        return "\"" + comment + "\"";
    }
}
