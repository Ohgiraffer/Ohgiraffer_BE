package com.ohgiraffer.auditlog.infrastructure.persistence;

import com.ohgiraffer.auditlog.domain.model.AuditLog;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_name", nullable = false, length = 50)
    private String domainName;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    private AuditLogJpaEntity(String domainName, String eventType, Long actorId, String targetId,
                              String beforeValue, String afterValue, String previousHash, String hash,
                              LocalDateTime occurredAt) {
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

    public static AuditLogJpaEntity from(AuditLog auditLog) {
        return new AuditLogJpaEntity(
                auditLog.getDomainName(),
                auditLog.getEventType(),
                auditLog.getActorId(),
                auditLog.getTargetId(),
                auditLog.getBeforeValue(),
                auditLog.getAfterValue(),
                auditLog.getPreviousHash(),
                auditLog.getHash(),
                auditLog.getOccurredAt()
        );
    }

    public AuditLog toDomain() {
        return new AuditLog(id, domainName, eventType, actorId, targetId, beforeValue, afterValue,
                previousHash, hash, occurredAt);
    }
}