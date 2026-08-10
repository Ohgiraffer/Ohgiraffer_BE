package com.ohgiraffer.consultation.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record SaveConsultationRecordRequest(
        @NotBlank String counselorNote
) {}