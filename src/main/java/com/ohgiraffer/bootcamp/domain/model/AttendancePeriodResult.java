package com.ohgiraffer.bootcamp.domain.model;

import java.time.LocalDate;

public record AttendancePeriodResult(
        Long id,
        Integer periodNo,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}