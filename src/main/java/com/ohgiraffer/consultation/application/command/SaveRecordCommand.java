package com.ohgiraffer.consultation.application.command;

public record SaveRecordCommand(
        Long consultationId,
        String counselorNote
) {}