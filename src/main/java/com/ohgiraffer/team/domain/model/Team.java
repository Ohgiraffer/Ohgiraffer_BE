package com.ohgiraffer.team.domain.model;

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