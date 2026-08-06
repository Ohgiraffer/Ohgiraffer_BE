package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStudentTeamRepository
        extends JpaRepository<TeamMemberJpaEntity, Long> {

    List<TeamMemberJpaEntity>
    findAllByUserIdAndLeftAtIsNull(Long userId);
}