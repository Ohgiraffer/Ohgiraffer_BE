package com.ohgiraffer.team.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TeamPeriod {

    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime archivedAt;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;

    private TeamPeriod(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime archivedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.archivedAt = archivedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
    }

    public static TeamPeriod create(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validatePeriod(
                startDate,
                endDate
        );

        return new TeamPeriod(
                null,
                startDate,
                endDate,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static TeamPeriod restore(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime archivedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        return new TeamPeriod(
                id,
                startDate,
                endDate,
                archivedAt,
                deletedAt,
                createdAt
        );
    }

    public TeamPeriod update(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateAssignable();

        validatePeriod(
                startDate,
                endDate
        );

        return new TeamPeriod(
                id,
                startDate,
                endDate,
                archivedAt,
                deletedAt,
                createdAt
        );
    }

    public TeamPeriod archive(
            LocalDateTime archivedAt
    ) {
        if (isDeleted()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DELETED
            );
        }

        if (isArchived()) {
            return this;
        }

        return new TeamPeriod(
                id,
                startDate,
                endDate,
                archivedAt,
                deletedAt,
                createdAt
        );
    }

    public TeamPeriod markDeleted(
            LocalDateTime deletedAt
    ) {
        if (isDeleted()) {
            return this;
        }

        if (!isArchived()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "보관되지 않은 팀 기간은 정리할 수 없습니다."
            );
        }

        return new TeamPeriod(
                id,
                startDate,
                endDate,
                archivedAt,
                deletedAt,
                createdAt
        );
    }

    public void validateAssignable() {
        if (isArchived()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_ARCHIVED
            );
        }

        if (isDeleted()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DELETED
            );
        }
    }

    private static void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null
                || endDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 기간 시작일과 종료일을 입력해주세요."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.TEAM_INVALID_PERIOD
            );
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}