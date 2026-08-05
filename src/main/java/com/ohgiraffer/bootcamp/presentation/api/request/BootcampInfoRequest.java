package com.ohgiraffer.bootcamp.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BootcampInfoRequest(
        @Schema(description = "운영 기관명")
        @NotBlank(message = "조직명은 필수입니다.")
        String orgName,

        @Schema(description = "부트캠프 과정명")
        @NotBlank(message = "과정명은 필수입니다.")
        String proName,

        @Schema(description = "부트캠프 시작일")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "부트캠프 종료일")
        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate
) {
    @AssertTrue(message = "종료일은 시작일보다 빠를 수 없습니다.")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }
}