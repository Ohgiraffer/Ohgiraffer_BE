package com.ohgiraffer.team.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Team {

    private final Long id;
    private final Long teamPeriodId;
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
            Long teamPeriodId,
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
        this.teamPeriodId = teamPeriodId;
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
            Long teamPeriodId,
            String name,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateTeamPeriodId(
                teamPeriodId
        );

        validateName(
                name
        );

        return new Team(
                null,
                teamPeriodId,
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
            Long teamPeriodId,
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
                teamPeriodId,
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

        return new Team(
                id,
                teamPeriodId,
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

    public Team markDeleted(
            LocalDateTime deletedAt
    ) {
        if (isDeleted()) {
            return this;
        }

        return new Team(
                id,
                teamPeriodId,
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

    public Team markChannelCleanupRequested(
            LocalDateTime channelDeletedAt
    ) {
        if (this.channelDeletedAt != null) {
            return this;
        }

        return new Team(
                id,
                teamPeriodId,
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

    public Team markWorkspaceCleanupRequested(
            LocalDateTime workspaceDeletedAt
    ) {
        if (this.workspaceDeletedAt != null) {
            return this;
        }

        return new Team(
                id,
                teamPeriodId,
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
        if (isDissolved()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DISSOLVED
            );
        }

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

    private static void validateTeamPeriodId(
            Long teamPeriodId
    ) {
        if (teamPeriodId == null
                || teamPeriodId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 기간 ID가 올바르지 않습니다."
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

    public Long getId() {
        return id;
    }

    public Long getTeamPeriodId() {
        return teamPeriodId;
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
}