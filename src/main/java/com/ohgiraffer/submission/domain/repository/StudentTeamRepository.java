package com.ohgiraffer.submission.domain.repository;

import java.util.Optional;

public interface StudentTeamRepository {

    Optional<Long> findActiveTeamIdByStudentId(
            Long studentId
    );
}