package com.ohgiraffer.evaluation.presentation.api.response;

import com.ohgiraffer.evaluation.application.query.EvaluationSyncResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "평가 시트 동기화 결과")
public record EvaluationSyncResponse(

        @Schema(
                description = """
                        이번 실행으로 남은 이력의 식별자.
                        바뀐 것이 없으면 이력을 만들지 않으므로 null 이다.
                        """,
                example = "1"
        )
        Long syncLogId,

        @Schema(description = "새로 저장된 평가 수", example = "12")
        int addedCount,

        @Schema(description = "내용이 바뀌어 갱신된 평가 수", example = "2")
        int updatedCount,

        @Schema(description = "추가와 갱신을 합친 수. 화면의 '건수'", example = "14")
        int changedCount,

        @Schema(
                description = "변경 내용 요약. 동기화 직후 화면에 그대로 보여주면 된다",
                example = "[수정] 박민준 · 중간평가 · 코드 품질 — 점수 70 → 88"
        )
        String diffSummary,

        @Schema(
                description = """
                        반영하지 못한 행. 한 행이 잘못돼도 나머지는 반영되므로,
                        여기 담긴 줄 번호를 화면에서 안내하면 사용자가 시트를 고칠 수 있다.
                        """
        )
        List<SkippedRowResponse> skipped
) {

    public static EvaluationSyncResponse from(EvaluationSyncResult result) {
        return new EvaluationSyncResponse(
                result.syncLogId(),
                result.addedCount(),
                result.updatedCount(),
                result.changedCount(),
                result.diffSummary(),
                result.skipped().stream()
                        .map(SkippedRowResponse::from)
                        .toList()
        );
    }

    @Schema(description = "반영하지 못한 행")
    public record SkippedRowResponse(

            @Schema(description = "시트에서 보이는 줄 번호. 헤더가 1행", example = "5")
            int rowNumber,

            @Schema(description = "건너뛴 이유", example = "훈련생을 찾을 수 없습니다: kim@campflow.test")
            String reason
    ) {

        public static SkippedRowResponse from(
                EvaluationSyncResult.SkippedRow skippedRow
        ) {
            return new SkippedRowResponse(
                    skippedRow.rowNumber(),
                    skippedRow.reason()
            );
        }
    }
}
