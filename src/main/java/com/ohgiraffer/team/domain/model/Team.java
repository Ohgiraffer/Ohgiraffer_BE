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

    private Team(
            Long id,
            String name,
            String sendbirdChannelUrl,
            String notionPageId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime dissolvedAt
    ) {
        this.id = id;
        this.name = name;
        this.sendbirdChannelUrl = sendbirdChannelUrl;
        this.notionPageId = notionPageId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dissolvedAt = dissolvedAt;
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
            LocalDateTime dissolvedAt
    ) {
        return new Team(
                id,
                name,
                sendbirdChannelUrl,
                notionPageId,
                startDate,
                endDate,
                dissolvedAt
        );
    }

    public Team update(
            String name,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (isDissolved()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DISSOLVED
            );
        }

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
                dissolvedAt
        );
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

    public boolean isDissolved() {
        return dissolvedAt != null;
    }
}