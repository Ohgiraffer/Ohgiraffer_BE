package com.ohgiraffer.evaluation.domain.model;

import java.math.BigDecimal;

/**
 * 동기화 한 번에서 바뀐 평가 하나.
 *
 * <p>어떤 평가가 어떻게 달라졌는지를 담는다. 이력에 남길 요약문을 만들 때 쓰고,
 * AI 요약을 붙일 때도 같은 값을 재료로 쓴다. 그래서 문장이 아니라 값으로 들고 있는다.
 *
 * <p>훈련생을 식별자가 아니라 이름으로 담는 이유는 이력을 읽는 사람 때문이다.
 * 나중에 이름이 바뀌어도 그때의 기록은 그때의 이름으로 남는 편이 자연스럽다.
 */
public record EvaluationChange(
        Type type,
        String traineeName,
        String evaluationType,
        String item,
        BigDecimal previousScore,
        BigDecimal newScore,
        String previousComment,
        String newComment
) {

    public enum Type {

        /** 시트에 새로 생긴 평가 */
        ADDED,

        /** 이미 있던 평가의 점수나 의견이 달라짐 */
        UPDATED
    }

    public static EvaluationChange added(
            String traineeName,
            EvaluationRecord record
    ) {
        return new EvaluationChange(
                Type.ADDED,
                traineeName,
                record.getEvaluationType(),
                record.getItem(),
                null,
                record.getScore(),
                null,
                record.getComment()
        );
    }

    public static EvaluationChange updated(
            String traineeName,
            EvaluationRecord before,
            EvaluationRecord after
    ) {
        return new EvaluationChange(
                Type.UPDATED,
                traineeName,
                after.getEvaluationType(),
                after.getItem(),
                before.getScore(),
                after.getScore(),
                before.getComment(),
                after.getComment()
        );
    }

    /**
     * 점수가 달라졌는지 여부.
     *
     * <p>{@code compareTo} 로 비교한다. {@code equals} 는 소수 자릿수까지 따져
     * 85 와 85.00 을 다르게 본다.
     */
    public boolean scoreChanged() {
        if (previousScore == null || newScore == null) {
            return previousScore != newScore;
        }

        return previousScore.compareTo(newScore) != 0;
    }

    public boolean commentChanged() {
        return !java.util.Objects.equals(previousComment, newComment);
    }
}
