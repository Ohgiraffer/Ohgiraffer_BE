package com.ohgiraffer.consultation.domain.model;

import java.time.LocalDate;

public record HistoryQuery(
        Long counselorId,
        ConsultationStatus status,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
