package com.ohgiraffer.bootcamp.domain.model;

import java.time.LocalDate;

public record BootcampPeriodResult(
        LocalDate startDate,
        LocalDate endDate
) {
}
