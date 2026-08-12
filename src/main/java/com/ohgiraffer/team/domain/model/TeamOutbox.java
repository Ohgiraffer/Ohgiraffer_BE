package com.ohgiraffer.team.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class TeamOutbox {

    private static final int MAX_RETRY_COUNT = 5;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final Long id;
    private final TeamOutboxType type;
    private final TeamOutboxStatus status;
    private final String payload;
    private final int retryCount;
    private final String lastErrorMessage;
    private final LocalDateTime nextRetryAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Long version;

    private TeamOutbox(
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

    public static TeamOutbox create(
            TeamOutboxType type,
            String payload,
            LocalDateTime now
    ) {
        validateType(
                type
        );

        validatePayload(
                payload
        );

        if (now == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox 생성 시간이 올바르지 않습니다."
            );
        }

        return new TeamOutbox(
                null,
                type,
                TeamOutboxStatus.PENDING,
                payload,
                0,
                null,
                now,
                now,
                now,
                null
        );
    }

    public static TeamOutbox restore(
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
        return new TeamOutbox(
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

    public TeamOutbox markProcessing(
            LocalDateTime now
    ) {
        validateId();

        return new TeamOutbox(
                id,
                type,
                TeamOutboxStatus.PROCESSING,
                payload,
                retryCount,
                lastErrorMessage,
                nextRetryAt,
                createdAt,
                now,
                version
        );
    }

    public TeamOutbox markSucceeded(
            LocalDateTime now
    ) {
        validateId();

        return new TeamOutbox(
                id,
                type,
                TeamOutboxStatus.SUCCEEDED,
                payload,
                retryCount,
                null,
                null,
                createdAt,
                now,
                version
        );
    }

    public TeamOutbox markFailed(
            String errorMessage,
            LocalDateTime now
    ) {
        validateId();

        int nextRetryCount =
                retryCount + 1;

        TeamOutboxStatus nextStatus =
                nextRetryCount >= MAX_RETRY_COUNT
                        ? TeamOutboxStatus.DEAD
                        : TeamOutboxStatus.FAILED;

        return new TeamOutbox(
                id,
                type,
                nextStatus,
                payload,
                nextRetryCount,
                trimErrorMessage(
                        errorMessage
                ),
                calculateNextRetryAt(
                        nextRetryCount,
                        now
                ),
                createdAt,
                now,
                version
        );
    }

    public boolean canRetry(
            LocalDateTime processingTimeoutAt
    ) {
        if (status == TeamOutboxStatus.PENDING
                || status == TeamOutboxStatus.FAILED) {
            return true;
        }

        return status == TeamOutboxStatus.PROCESSING
                && updatedAt != null
                && processingTimeoutAt != null
                && !updatedAt.isAfter(
                processingTimeoutAt
        );
    }

    private LocalDateTime calculateNextRetryAt(
            int retryCount,
            LocalDateTime now
    ) {
        if (retryCount >= MAX_RETRY_COUNT) {
            return null;
        }

        long delaySeconds =
                Math.min(
                        300L,
                        (long) Math.pow(
                                2,
                                retryCount
                        ) * 10L
                );

        return now.plusSeconds(
                delaySeconds
        );
    }

    private static void validateType(
            TeamOutboxType type
    ) {
        if (type == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox 타입이 올바르지 않습니다."
            );
        }
    }

    private static void validatePayload(
            String payload
    ) {
        if (payload == null
                || payload.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox payload가 올바르지 않습니다."
            );
        }
    }

    private void validateId() {
        if (id == null
                || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox ID가 올바르지 않습니다."
            );
        }
    }

    private String trimErrorMessage(
            String errorMessage
    ) {
        if (errorMessage == null
                || errorMessage.isBlank()) {
            return null;
        }

        if (errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }

        return errorMessage.substring(
                0,
                MAX_ERROR_MESSAGE_LENGTH
        );
    }

    public Long getId() {
        return id;
    }

    public TeamOutboxType getType() {
        return type;
    }

    public TeamOutboxStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}