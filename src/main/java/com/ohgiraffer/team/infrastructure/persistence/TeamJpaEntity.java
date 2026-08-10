package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_period_id")
    private Long teamPeriodId;

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "sendbird_channel_url",
            length = 255
    )
    private String sendbirdChannelUrl;

    @Column(
            name = "notion_page_id",
            length = 100
    )
    private String notionPageId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "dissolved_at")
    private LocalDateTime dissolvedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "channel_deleted_at")
    private LocalDateTime channelDeletedAt;

    @Column(name = "workspace_deleted_at")
    private LocalDateTime workspaceDeletedAt;

    private TeamJpaEntity(
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

    public static TeamJpaEntity from(
            Team team
    ) {
        return new TeamJpaEntity(
                team.getId(),
                team.getTeamPeriodId(),
                team.getName(),
                team.getSendbirdChannelUrl(),
                team.getNotionPageId(),
                team.getStartDate(),
                team.getEndDate(),
                team.getDissolvedAt(),
                team.getArchivedAt(),
                team.getDeletedAt(),
                team.getChannelDeletedAt(),
                team.getWorkspaceDeletedAt()
        );
    }

    public Team toDomain() {
        return Team.restore(
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
}