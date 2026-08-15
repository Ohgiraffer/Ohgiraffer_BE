package com.ohgiraffer.evaluation.presentation.api.response;

import com.ohgiraffer.evaluation.domain.model.TraineeChangeSummary;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 훈련생 한 명의 변경 카드. 화면의 'AI 수정사항 요약' 한 칸과 대응한다.
 */
@Schema(description = "훈련생별 변경 요약")
public record TraineeChangeSummaryResponse(

        @Schema(description = "훈련생 이름", example = "김철수")
        String traineeName,

        @Schema(
                description = "바뀐 평가의 평가 유형. 여러 개면 쉼표로 이어진다",
                example = "중간평가"
        )
        String evaluationType,

        @Schema(
                description = "바뀐 평가 항목. 여러 개면 쉼표로 이어진다",
                example = "코드 품질, 발표"
        )
        String item,

        @Schema(
                description = """
                        점수 변화. 바뀐 항목이 하나면 '70 → 88', 여럿이면
                        '코드 품질 70 → 88 / 발표 85 신규' 처럼 항목을 앞에 붙인다.
                        점수가 안 바뀌었으면 '변경 없음' 이다.
                        """,
                example = "70 → 88"
        )
        String score,

        @Schema(
                description = "의견 변화. 형식은 점수와 같다",
                example = "(비어 있음) → \"코드 품질이 향상되었습니다\""
        )
        String comment,

        @Schema(
                description = """
                        운영진이 확인해야 할 점. AI 가 짚어낸 것이라 대개 null 이다.
                        AI 호출이 실패해도 null 이 되며, 나머지 칸은 그대로 채워진다.
                        null 이면 화면에서 그 줄을 그리지 않는다.
                        """,
                example = "3주차 과제 제출 누락 여부 재확인 필요"
        )
        String needsCheck
) {

    public static TraineeChangeSummaryResponse from(TraineeChangeSummary summary) {
        return new TraineeChangeSummaryResponse(
                summary.traineeName(),
                summary.evaluationType(),
                summary.item(),
                summary.score(),
                summary.comment(),
                summary.needsCheck()
        );
    }
}
