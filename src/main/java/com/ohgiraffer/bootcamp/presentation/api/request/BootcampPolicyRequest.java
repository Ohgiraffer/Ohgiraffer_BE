package com.ohgiraffer.bootcamp.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BootcampPolicyRequest (

        @Schema(description = "부트캠프 ID")
        @NotNull(message = "부트캠프 ID는 필수입니다.")
        Long bootcampId,

        @Schema(description = "단위기간 목록")
        @NotEmpty(message = "단위기간은 최소 1개 이상이어야 합니다.")
        @Valid
        List<PeriodRequest> periods,

        @Schema(description = "주의 기준 출석률")
        @NotNull(message = "주의 기준 출석률은 필수입니다.")
        @DecimalMin(value = "0.0", message = "출석률은 0 이상이어야 합니다.")
        @DecimalMax(value = "100.0", message = "출석률은 100 이하여야 합니다.")
        BigDecimal cautionPercent,

        @Schema(description = "경고 기준 출석률")
        @NotNull(message = "경고 기준 출석률은 필수입니다.")
        @DecimalMin(value = "0.0", message = "출석률은 0 이상이어야 합니다.")
        @DecimalMax(value = "100.0", message = "출석률은 100 이하여야 합니다.")
        BigDecimal warningPercent,

        @Schema(description = "제적위험 기준 출석률")
        @NotNull(message = "제적위험 기준 출석률은 필수입니다.")
        @DecimalMin(value = "0.0", message = "출석률은 0 이상이어야 합니다.")
        @DecimalMax(value = "100.0", message = "출석률은 100 이하여야 합니다.")
        BigDecimal expulsionPercent
) {
    public record PeriodRequest(
            @Schema(description = "단위기간 번호")
            @NotNull(message = "단위기간 번호는 필수입니다.")
            Integer periodNo,

            @Schema(description = "시작일")
            @NotNull(message = "시작일은 필수입니다.")
            LocalDate periodStart,

            @Schema(description = "종료일")
            @NotNull(message = "종료일은 필수입니다.")
            LocalDate periodEnd
    ) {
    }
}