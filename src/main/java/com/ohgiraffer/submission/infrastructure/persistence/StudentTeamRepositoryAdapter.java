package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentTeamRepositoryAdapter
        implements StudentTeamRepository {

    private final SpringDataStudentTeamRepository repository;

    @Override
    public Optional<Long> findTeamIdByStudentIdAndDateTime(
            Long studentId,
            LocalDateTime targetAt
    ) {
        if (studentId == null
                || studentId <= 0
                || targetAt == null) {
            return Optional.empty();
        }

        List<Long> teamIds =
                repository.findTeamIdsByUserIdAndDateTime(
                        studentId,
                        targetAt
                );

        /*
         * 동일한 기준 일시에 여러 팀에 소속된 결과가 나온다면
         * 임의로 하나를 선택하지 않고 데이터 불일치로 처리합니다.
         */
        if (teamIds.size() > 1) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_TEAM_DATA_INCONSISTENT
            );
        }

        return teamIds.stream()
                .findFirst();
    }
}