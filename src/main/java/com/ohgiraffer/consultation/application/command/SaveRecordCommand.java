package com.ohgiraffer.consultation.application.command;

public record SaveRecordCommand(
        Long consultationId,
        Long callerId,
        String counselorNote
) {}