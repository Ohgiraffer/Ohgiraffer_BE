package com.ohgiraffer.auditlog.domain.model;

import java.time.LocalDateTime;

public class AuditLog {

    private final Long id;
    private final String domainName;
    private final String eventType;
    private final Long actorId;
    private final String targetId;
    private final String beforeValue;
    private final String afterValue;
    private final String previousHash;
    private final String hash;
    private final LocalDateTime occurredAt;

    public AuditLog(Long id, String domainName, String eventType, Long actorId, String targetId,
                    String beforeValue, String afterValue, String previousHash, String hash,
                    LocalDateTime occurredAt) {
        this.id = id;
        this.domainName = domainName;
        this.eventType = eventType;
        this.actorId = actorId;
        this.targetId = targetId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.previousHash = previousHash;
        this.hash = hash;
        this.occurredAt = occurredAt;
    }

    public static AuditLog create(String domainName, String eventType, Long actorId, String targetId,
                                  String beforeValue, String afterValue, String previousHash, String hash,
                                  LocalDateTime occurredAt) {
        return new AuditLog(null, domainName, eventType, actorId, targetId, beforeValue, afterValue,
                previousHash, hash, occurredAt);
    }

    public String buildRawPayload() {
        return String.join("|",
                previousHash,
                domainName,
                eventType,
                String.valueOf(actorId),
                nullToEmpty(targetId),
                nullToEmpty(beforeValue),
                nullToEmpty(afterValue),
                occurredAt.toString()
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public Long getId() { return id; }
    public String getDomainName() { return domainName; }
    public String getEventType() { return eventType; }
    public Long getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getBeforeValue() { return beforeValue; }
    public String getAfterValue() { return afterValue; }
    public String getPreviousHash() { return previousHash; }
    public String getHash() { return hash; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}