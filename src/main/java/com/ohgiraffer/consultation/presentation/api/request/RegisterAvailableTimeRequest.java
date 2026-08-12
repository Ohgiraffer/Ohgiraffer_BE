package com.ohgiraffer.consultation.presentation.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RegisterAvailableTimeRequest(
        @NotNull LocalDate date,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        List<LocalTime> times
) {}