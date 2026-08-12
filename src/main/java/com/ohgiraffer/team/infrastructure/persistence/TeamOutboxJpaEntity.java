package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxStatus;
import com.ohgiraffer.team.domain.model.TeamOutboxType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_outbox_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 50
    )
    private TeamOutboxType type;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private TeamOutboxStatus status;

    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Column(
            name = "retry_count",
            nullable = false
    )
    private int retryCount;

    @Column(
            name = "last_error_message",
            length = 1000
    )
    private String lastErrorMessage;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    private TeamOutboxJpaEntity(
            Long id,
            TeamOutboxType type,
            TeamOutboxStatus status,
            String payload,
            int retryCount,
            String lastErrorMessage,
            LocalDateTime nextRetryAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long version
    ) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.payload = payload;
        this.retryCount = retryCount;
        this.lastErrorMessage = lastErrorMessage;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static TeamOutboxJpaEntity from(
            TeamOutbox outbox
    ) {
        return new TeamOutboxJpaEntity(
                outbox.getId(),
                outbox.getType(),
                outbox.getStatus(),
                outbox.getPayload(),
                outbox.getRetryCount(),
                outbox.getLastErrorMessage(),
                outbox.getNextRetryAt(),
                outbox.getCreatedAt(),
                outbox.getUpdatedAt(),
                outbox.getVersion()
        );
    }

    public TeamOutbox toDomain() {
        return TeamOutbox.restore(
                id,
                type,
                status,
                payload,
                retryCount,
                lastErrorMessage,
                nextRetryAt,
                createdAt,
                updatedAt,
                version
        );
    }
}