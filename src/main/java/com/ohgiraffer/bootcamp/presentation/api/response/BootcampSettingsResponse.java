package com.ohgiraffer.bootcamp.presentation.api.response;

import java.time.LocalDate;
import java.util.List;

public record BootcampSettingsResponse(
        Long bootcampId,
        String orgName,
        String proName,
        LocalDate startDate,
        LocalDate endDate,
        List<PeriodItem> periods
) {
    public record PeriodItem(
            Long id,
            Integer periodNo,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {}
}