package com.ohgiraffer.bootcamp.application.command;

import java.time.LocalDate;

public record PeriodCommand(
        Integer periodNo,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
