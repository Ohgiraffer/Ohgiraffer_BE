package com.ohgiraffer.team.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Team {

    private final Long id;
    private final String name;
    private final String sendbirdChannelUrl;
    private final String notionPageId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime dissolvedAt;
    private final LocalDateTime archivedAt;
    private final LocalDateTime deletedAt;
    private final LocalDateTime channelDeletedAt;
    private final LocalDateTime workspaceDeletedAt;

    private Team(
            Long id,
            String name,
            String sendbirdChannelUrl,
            String notionPageId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime dissolvedAt,
            LocalDateTime archivedAt,
            LocalDateTime deletedAt,
            LocalDateTime channelDeletedAt,
            LocalDateTime workspaceDeletedAt
    ) {
        this.id = id;
        this.name = name;
        this.sendbirdChannelUrl = sendbirdChannelUrl;
        this.notionPageId = notionPageId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dissolvedAt = dissolvedAt;
        this.archivedAt = archivedAt;
        this.deletedAt = deletedAt;
        this.channelDeletedAt = channelDeletedAt;
        this.workspaceDeletedAt = workspaceDeletedAt;
    }

    public static Team create(
            String name,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateName(
                name
        );

        validatePeriod(
                startDate,
                endDate
        );

        return new Team(
                null,
                name.trim(),
                null,
                null,
                startDate,
                endDate,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Team restore(
            Long id,
            String name,
            String sendbirdChannelUrl,
            String notionPageId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime dissolvedAt,
            LocalDateTime archivedAt,
            LocalDateTime deletedAt,
            LocalDateTime channelDeletedAt,
            LocalDateTime workspaceDeletedAt
    ) {
        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public Team update(
            String name,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateAssignable();

        validateName(
                name
        );

        validatePeriod(
                startDate,
                endDate
        );

        return new Team(
                id,
                name.trim(),
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public Team archive(
            LocalDateTime archivedAt
    ) {
        if (isDeleted()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DISSOLVED
            );
        }

        if (isArchived()) {
            return this;
        }

        if (archivedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 보관 일시가 올바르지 않습니다."
            );
        }

        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public Team markDeleted(
            LocalDateTime deletedAt
    ) {
        if (isDeleted()) {
            return this;
        }

        if (!isArchived()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "보관되지 않은 팀은 정리할 수 없습니다."
            );
        }

        if (deletedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 삭제 일시가 올바르지 않습니다."
            );
        }

        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public Team markChannelDeleted(
            LocalDateTime channelDeletedAt
    ) {
        if (this.channelDeletedAt != null) {
            return this;
        }

        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public Team markWorkspaceDeleted(
            LocalDateTime workspaceDeletedAt
    ) {
        if (this.workspaceDeletedAt != null) {
            return this;
        }

        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt,
                archivedAt,
                deletedAt,
                channelDeletedAt,
                workspaceDeletedAt
        );
    }

    public void validateAssignable() {
        if (!isAssignable()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DISSOLVED
            );
        }
    }

    private static void validateName(
            String name
    ) {
        if (name == null
                || name.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀명을 입력해주세요."
            );
        }

        if (name.trim().length() > 100) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀명은 100자를 초과할 수 없습니다."
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
                    "팀 시작일과 종료일을 입력해주세요."
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

    public String getName() {
        return name;
    }

    public String getSendbirdChannelUrl() {
        return sendbirdChannelUrl;
    }

    public String getNotionPageId() {
        return notionPageId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getDissolvedAt() {
        return dissolvedAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getChannelDeletedAt() {
        return channelDeletedAt;
    }

    public LocalDateTime getWorkspaceDeletedAt() {
        return workspaceDeletedAt;
    }

    public boolean isDissolved() {
        return dissolvedAt != null;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAssignable() {
        return !isDissolved()
                && !isArchived()
                && !isDeleted();
    }
}