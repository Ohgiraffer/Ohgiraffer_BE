package com.ohgiraffer.evaluation.presentation.api.request;

import com.ohgiraffer.evaluation.application.command.SaveEvaluationSheetLinkCommand;
import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "평가 시트 연동 설정 저장 요청")
public record SaveEvaluationSheetLinkRequest(

        @Schema(
                description = "구글 스프레드시트 주소",
                example = "https://docs.google.com/spreadsheets/d/1AbCdEf123/edit"
        )
        @NotBlank(message = "스프레드시트 주소는 필수입니다.")
        String spreadsheetUrl,

        @Schema(
                description = """
                        읽어올 시트 탭 이름. 생략하면 첫 번째 탭을 쓴다.
                        화면에 탭 선택이 없으면 보내지 않아도 된다.
                        """,
                example = "시트1"
        )
        String tabName,

        @Schema(description = "컬럼 매핑")
        @NotNull(message = "컬럼 매핑은 필수입니다.")
        @Valid
        ColumnMappingRequest columnMapping
) {

    public SaveEvaluationSheetLinkCommand toCommand() {
        return new SaveEvaluationSheetLinkCommand(
                spreadsheetUrl,
                tabName,
                columnMapping.toDomain()
        );
    }

    /**
     * 시트의 어느 컬럼이 우리의 어느 값인지.
     *
     * <p>값은 시트에 실제로 있는 컬럼 이름이다. 연결 확인 API 응답의 {@code columns} 에서
     * 고른 것을 그대로 보내면 된다.
     */
    @Schema(description = "컬럼 매핑")
    public record ColumnMappingRequest(

            @Schema(description = "훈련생을 찾을 컬럼. 값은 이메일로 해석한다", example = "이메일")
            @NotBlank(message = "훈련생 식별자 컬럼은 필수입니다.")
            String traineeIdentifier,

            @Schema(description = "평가 유형 컬럼", example = "평가유형")
            @NotBlank(message = "평가 유형 컬럼은 필수입니다.")
            String evaluationType,

            @Schema(description = "평가 항목 컬럼", example = "평가항목")
            @NotBlank(message = "평가 항목 컬럼은 필수입니다.")
            String item,

            @Schema(description = "점수 컬럼", example = "점수")
            @NotBlank(message = "점수 컬럼은 필수입니다.")
            String score,

            @Schema(description = "의견 컬럼. 선택", example = "의견")
            String comment
    ) {

        public EvaluationColumnMapping toDomain() {
            return new EvaluationColumnMapping(
                    traineeIdentifier,
                    evaluationType,
                    item,
                    score,
                    comment
            );
        }
    }
}
