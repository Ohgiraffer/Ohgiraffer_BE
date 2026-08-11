package com.ohgiraffer.consultation.application.command;

import java.time.LocalDateTime;

public record RequestConsultationCommand(
        Long requesterId,
        Long counselorId,
        LocalDateTime scheduledAt,
        String topic,
        String content
) {}