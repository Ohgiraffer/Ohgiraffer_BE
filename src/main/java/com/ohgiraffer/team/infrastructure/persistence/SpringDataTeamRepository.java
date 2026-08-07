package com.ohgiraffer.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTeamRepository
        extends JpaRepository<TeamJpaEntity, Long> {

    List<TeamJpaEntity> findAllByOrderByIdAsc();
}