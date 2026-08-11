package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.AvailableTimeSlot;

import java.time.LocalTime;

public record AvailableTimeResponse(
        LocalTime time,
        boolean isReserved
) {
    public static AvailableTimeResponse from(AvailableTimeSlot slot) {
        return new AvailableTimeResponse(slot.time(), slot.isReserved());
    }
}