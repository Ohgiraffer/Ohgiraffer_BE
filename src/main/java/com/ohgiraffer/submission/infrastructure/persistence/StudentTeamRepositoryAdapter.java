package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentTeamRepositoryAdapter
        implements StudentTeamRepository {

    private final SpringDataStudentTeamRepository repository;

    @Override
    public Optional<Long> findActiveTeamIdByStudentId(
            Long studentId
    ) {
        List<TeamMemberJpaEntity> memberships =
                repository.findAllByUserIdAndLeftAtIsNull(
                        studentId
                );

        if (memberships.size() > 1) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_TEAM_DATA_INCONSISTENT
            );
        }

        return memberships.stream()
                .findFirst()
                .map(TeamMemberJpaEntity::getTeamId);
    }
}