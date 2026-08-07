package com.ohgiraffer.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTeamRepository
        extends JpaRepository<TeamJpaEntity, Long> {
}