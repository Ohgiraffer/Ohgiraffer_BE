package com.ohgiraffer.bootcamp.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record BootcampSettingsUpdateRequest(
        @NotBlank String orgName,
        @NotBlank String proName,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty @Valid List<@NotNull PeriodItem> periods
) {
    public record PeriodItem(
            @NotNull Integer periodNo,
            @NotNull LocalDate periodStart,
            @NotNull LocalDate periodEnd
    ) {}
}