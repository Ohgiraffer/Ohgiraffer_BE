package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.domain.model.EvaluationChange;

import java.math.BigDecimal;
import java.util.List;

/**
 * 변경 내역을 제미나이에게 보낼 글로 바꾼다.
 *
 * <p>무엇이 바뀌었는지는 묻지 않는다. 그 값은 이미 우리가 들고 있어 화면에 그대로 적는다.
 * 여기서 묻는 것은 "이 중에 사람이 들여다봐야 할 대목이 있는가" 하나다.
 *
 * <p>답을 JSON 으로 받는다. 훈련생별 카드에 한 줄씩 끼워 넣어야 해서, 문장으로 받으면
 * 누구에 대한 이야기인지 다시 갈라내야 한다.
 */
final class EvaluationSummaryPromptBuilder {

    private EvaluationSummaryPromptBuilder() {
    }

    static String build(List<EvaluationChange> changes) {
        return """
                너는 부트캠프 운영진을 돕는 조수다.
                아래는 구글 시트에서 평가 데이터를 가져왔을 때 바뀐 내용이다.
                이 중에서 운영진이 한 번 더 확인해 봐야 할 훈련생을 골라라.

                JSON 배열만 출력해라. 설명, 인사말, 코드 블록 표시를 붙이지 마라.
                짚을 것이 없으면 빈 배열 [] 만 출력해라.

                배열의 각 원소는 이런 모양이다.
                { "traineeName": "훈련생 이름", "needsCheck": "확인해야 할 점" }

                이런 경우를 짚어라.
                - 점수가 0점이거나 지나치게 낮은데 의견에 사유가 없다.
                - 점수가 갑자기 크게 오르거나 내렸는데 설명이 없다.
                - 점수가 비어 있다. 입력을 빠뜨렸을 수 있다.
                - 의견과 점수가 서로 어긋난다. 칭찬하는 의견인데 점수가 낮은 경우다.

                지켜야 할 것.
                - 확인할 것이 없는 훈련생은 배열에 넣지 마라. 모두를 채우려 하지 마라.
                - needsCheck 는 한 문장으로 짧게 써라.
                - 데이터에 없는 내용을 지어내지 마라. 이름은 아래 목록에 있는 것만 쓴다.
                - 점수가 오른 것 자체는 확인할 일이 아니다. 사유가 없을 때만 짚어라.

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
            return "(비어 있음)";
        }

        return score.stripTrailingZeros().toPlainString();
    }

    /**
     * 값이 비었음을 나타내는 표시는 따옴표 밖에 둔다. 시트에 "없음" 이라고 적힌 값과
     * 값이 비어 있는 경우를 모델이 구분할 수 있어야 한다.
     */
    private static String text(String comment) {
        if (comment == null || comment.isBlank()) {
            return "(비어 있음)";
        }

        return "\"" + comment + "\"";
    }
}
