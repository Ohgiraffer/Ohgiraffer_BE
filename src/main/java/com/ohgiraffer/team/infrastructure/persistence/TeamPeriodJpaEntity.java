package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamPeriod;
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
@Table(name = "team_period")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPeriodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_period_id")
    private Long id;

    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;

    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private TeamPeriodJpaEntity(
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

    public static TeamPeriodJpaEntity from(
            TeamPeriod teamPeriod
    ) {
        return new TeamPeriodJpaEntity(
                teamPeriod.getId(),
                teamPeriod.getStartDate(),
                teamPeriod.getEndDate(),
                teamPeriod.getArchivedAt(),
                teamPeriod.getDeletedAt(),
                teamPeriod.getCreatedAt()
        );
    }

    public TeamPeriod toDomain() {
        return TeamPeriod.restore(
                id,
                startDate,
                endDate,
                archivedAt,
                deletedAt,
                createdAt
        );
    }
}