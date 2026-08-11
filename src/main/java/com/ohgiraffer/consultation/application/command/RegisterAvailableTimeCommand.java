package com.ohgiraffer.consultation.application.command;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RegisterAvailableTimeCommand(
        Long counselorId,
        LocalDate date,
        List<LocalTime> times
) {}