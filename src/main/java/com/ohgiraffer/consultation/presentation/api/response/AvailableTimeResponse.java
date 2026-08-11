package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.AvailableTimeSlot;

import java.time.format.DateTimeFormatter;

public record AvailableTimeResponse(
        String time,
        boolean isReserved
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static AvailableTimeResponse from(AvailableTimeSlot slot) {
        return new AvailableTimeResponse(
                slot.time().format(TIME_FORMATTER),
                slot.isReserved()
        );
    }
}