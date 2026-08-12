package com.ohgiraffer.consultation.domain.model;

import java.time.LocalTime;

public record AvailableTimeSlot(
        LocalTime time,
        boolean isReserved
) {
}
