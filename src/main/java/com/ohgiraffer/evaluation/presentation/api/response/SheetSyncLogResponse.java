package com.ohgiraffer.evaluation.presentation.api.response;

import com.ohgiraffer.evaluation.application.query.SheetSyncLogView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 동기화 이력 한 건. 목록과 상세가 같은 형식이다.
 *
 * <p>목록에서는 {@code diffSummary} 를 접어 두고 상세에서 펼치면 된다. 이력이 사람이
 * 버튼을 누를 때만, 그것도 변경이 있을 때만 쌓여 목록이 길지 않다.
 */
@Schema(description = "평가 시트 동기화 이력")
public record SheetSyncLogResponse(

        @Schema(description = "이력 식별자", example = "1")
        Long syncLogId,

        @Schema(
                description = """
                        실행한 사람 이름. 탈퇴했거나 시스템이 실행한 경우 null 이다.
                        화면에서는 '시스템' 처럼 대체 문구를 보여주면 된다.
                        """,
                example = "이매니저"
        )
        String executedByName,

        @Schema(description = "추가와 수정을 합친 변경 건수", example = "4")
        int changedCount,

        @Schema(
                description = "변경 내용 요약",
                example = "[수정] 박민준 · 중간평가 · 코드 품질 — 점수 70 → 88"
        )
        String diffSummary,

        @Schema(description = "실행 시각 (KST)", example = "2026-08-10T14:30:00")
        LocalDateTime syncedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static SheetSyncLogResponse from(SheetSyncLogView view) {
        return new SheetSyncLogResponse(
                view.syncLogId(),
                view.executedByName(),
                view.changedCount(),
                view.diffSummary(),
                toKst(view.syncedAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}
