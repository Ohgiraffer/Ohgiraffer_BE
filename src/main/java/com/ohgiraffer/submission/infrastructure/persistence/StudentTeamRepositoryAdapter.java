package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentTeamRepositoryAdapter
        implements StudentTeamRepository {

    private final SpringDataStudentTeamRepository repository;

    @Override
    public Optional<Long> findTeamIdByStudentIdAndDate(
            Long studentId,
            LocalDate targetDate
    ) {
        if (studentId == null
                || studentId <= 0
                || targetDate == null) {
            return Optional.empty();
        }

        List<Long> teamIds =
                repository.findTeamIdsByUserIdAndDate(
                        studentId,
                        targetDate
                );

        /*
         * 한 훈련생이 동일한 팀 운영 기간에 여러 팀에 소속되어 있다면
         * 임의의 팀을 선택하지 않고 데이터 불일치로 처리합니다.
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