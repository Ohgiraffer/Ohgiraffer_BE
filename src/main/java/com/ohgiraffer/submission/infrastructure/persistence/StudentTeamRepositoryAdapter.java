package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentTeamRepositoryAdapter
        implements StudentTeamRepository {

    private final SpringDataStudentTeamRepository repository;
    private final Clock clock;

    /**
     * 제출물 관리에서 사용할 훈련생의 현재 활성 팀을 반환합니다.
     *
     * 팀 제출 생성, 제출함 목록, 제출함 상세,
     * 제출물 수정이 모두 이 메서드를 통해 같은 팀 판정 기준을 사용합니다.
     */
    @Override
    public Optional<Long> findActiveTeamIdByStudentId(
            Long studentId
    ) {
        if (studentId == null
                || studentId <= 0) {
            return Optional.empty();
        }

        LocalDate currentDate =
                LocalDate.now(clock);

        List<Long> teamIds =
                repository.findActiveTeamIdsByUserId(
                        studentId,
                        currentDate
                );

        /*
         * 현재 활성 팀은 한 사용자당 최대 하나여야 합니다.
         * DB 유니크 제약이 있더라도 잘못된 데이터가 유입됐을 때
         * 임의의 팀을 선택하지 않고 명확하게 실패시킵니다.
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