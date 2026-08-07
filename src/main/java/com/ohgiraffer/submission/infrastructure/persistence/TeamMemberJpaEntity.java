package com.ohgiraffer.submission.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_member")
public class TeamMemberJpaEntity {

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

    protected TeamMemberJpaEntity() {
    }

    public Long getTeamId() {
        return teamId;
    }
}