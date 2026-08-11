package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.domain.model.EvaluationChange;

import java.math.BigDecimal;
import java.util.List;

/**
 * 변경 내역을 제미나이에게 보낼 글로 바꾼다.
 *
 * <p>요구사항이 "단순 셀 변경 목록이 아니라 점수 변화, 주요 의견과 확인이 필요한 항목
 * 중심" 이라, 무엇을 중요하게 볼지를 지시에 적는다. 변경 내역만 던지면 모델이
 * 있는 그대로 나열하기 쉽다.
 */
final class EvaluationSummaryPromptBuilder {

    private EvaluationSummaryPromptBuilder() {
    }

    static String build(List<EvaluationChange> changes) {
        return """
                너는 부트캠프 운영진을 돕는 조수다.
                아래는 구글 시트에서 평가 데이터를 가져왔을 때 바뀐 내용이다.
                운영진이 빠르게 파악할 수 있도록 한국어로 요약해라.

                이렇게 써라.
                - 점수가 크게 오르거나 내린 훈련생을 먼저 짚어라.
                - 의견이 달라진 경우 무엇이 어떻게 바뀌었는지 한 줄로 적어라.
                - 확인이 필요해 보이는 항목이 있으면 마지막에 따로 언급해라.
                - 전체 3~6문장으로 줄여라. 변경을 하나씩 나열하지 마라.
                - 표나 목록 기호 없이 문장으로만 써라.
                - 데이터에 없는 내용을 지어내지 마라.

                변경 내역:
                %s
                """.formatted(describe(changes));
    }

    private static String describe(List<EvaluationChange> changes) {
        StringBuilder description = new StringBuilder();

        for (EvaluationChange change : changes) {
            description.append(describeOne(change))
                    .append(System.lineSeparator());
        }

        return description.toString();
    }

    private static String describeOne(EvaluationChange change) {
        if (change.type() == EvaluationChange.Type.ADDED) {
            return "추가 | %s | %s | %s | 점수 %s | 의견 %s".formatted(
                    change.traineeName(),
                    change.evaluationType(),
                    change.item(),
                    format(change.newScore()),
                    text(change.newComment())
            );
        }

        return "수정 | %s | %s | %s | 점수 %s -> %s | 의견 %s -> %s".formatted(
                change.traineeName(),
                change.evaluationType(),
                change.item(),
                format(change.previousScore()),
                format(change.newScore()),
                text(change.previousComment()),
                text(change.newComment())
        );
    }

    private static String format(BigDecimal score) {
        if (score == null) {
            return "없음";
        }

        return score.stripTrailingZeros().toPlainString();
    }

    private static String text(String comment) {
        if (comment == null || comment.isBlank()) {
            return "없음";
        }

        return comment;
    }
}
