package com.ohgiraffer.consultation.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RequestConsultationRequest(
        @NotNull Long counselorId,
        @NotNull LocalDateTime scheduledAt,
        @NotBlank String topic,
        @NotBlank String content
) {}