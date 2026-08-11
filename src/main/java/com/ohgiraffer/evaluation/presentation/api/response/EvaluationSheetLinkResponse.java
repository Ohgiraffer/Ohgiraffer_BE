package com.ohgiraffer.evaluation.presentation.api.response;

import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 평가 시트 연동 설정. 시각은 서버에서 한국 시간으로 변환해 내려준다.
 */
@Schema(description = "평가 시트 연동 설정")
public record EvaluationSheetLinkResponse(

        @Schema(description = "연동 식별자", example = "1")
        Long sheetLinkId,

        @Schema(description = "구글 스프레드시트 주소")
        String spreadsheetUrl,

        @Schema(description = "읽어오는 시트 탭", example = "시트1")
        String tabName,

        @Schema(description = "컬럼 매핑")
        ColumnMappingResponse columnMapping,

        @Schema(
                description = """
                        마지막으로 동기화한 시각 (KST). 한 번도 실행하지 않았으면 null 이다.
                        """,
                example = "2026-08-10T14:30:00"
        )
        LocalDateTime lastSyncedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static EvaluationSheetLinkResponse from(EvaluationSheetLink sheetLink) {
        return new EvaluationSheetLinkResponse(
                sheetLink.getId(),
                sheetLink.getSheetUrl(),
                sheetLink.getTabName(),
                ColumnMappingResponse.from(sheetLink.getColumnMapping()),
                toKst(sheetLink.getLastSyncedAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }

    @Schema(description = "컬럼 매핑")
    public record ColumnMappingResponse(
            String traineeIdentifier,
            String evaluationType,
            String item,
            String score,
            String comment
    ) {

        public static ColumnMappingResponse from(
                EvaluationColumnMapping columnMapping
        ) {
            if (columnMapping == null) {
                return null;
            }

            return new ColumnMappingResponse(
                    columnMapping.traineeIdentifier(),
                    columnMapping.evaluationType(),
                    columnMapping.item(),
                    columnMapping.score(),
                    columnMapping.comment()
            );
        }
    }
}
