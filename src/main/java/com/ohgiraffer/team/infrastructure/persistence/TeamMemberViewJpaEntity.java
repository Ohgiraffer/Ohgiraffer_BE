package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMemberViewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @Column(
            name = "team_id",
            nullable = false
    )
    private Long teamId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "joined_at",
            nullable = false
    )
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    private TeamMemberViewJpaEntity(
            Long id,
            Long teamId,
            Long userId,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public static TeamMemberViewJpaEntity from(
            TeamMember teamMember
    ) {
        return new TeamMemberViewJpaEntity(
                teamMember.getId(),
                teamMember.getTeamId(),
                teamMember.getUserId(),
                teamMember.getJoinedAt(),
                teamMember.getLeftAt()
        );
    }

    public TeamMember toDomain() {
        return TeamMember.restore(
                id,
                teamId,
                userId,
                null,
                null,
                null,
                joinedAt,
                leftAt
        );
    }
}