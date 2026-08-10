package com.ohgiraffer.bootcamp.domain.model;

import java.time.LocalDate;

public record AttendancePeriodStartResult(
        Long id,
        Long bootcampId,
        Integer periodNo,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}