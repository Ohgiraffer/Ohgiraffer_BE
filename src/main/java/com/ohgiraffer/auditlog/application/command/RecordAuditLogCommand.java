package com.ohgiraffer.auditlog.application.command;

public record RecordAuditLogCommand(
        String domainName,
        String eventType,
        Long actorId,
        String targetId,
        String beforeValue,
        String afterValue
) {
}
