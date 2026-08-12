package com.ohgiraffer.submission.domain.repository;

import java.time.LocalDate;
import java.util.Optional;

public interface StudentTeamRepository {

    /**
     * 기준 날짜가 포함되는 팀 운영 기간을 기준으로
     * 훈련생의 소속 팀 ID를 조회합니다.
     *
     * @param studentId 훈련생 ID
     * @param targetDate 팀 소속을 판단할 기준 날짜
     */
    Optional<Long> findTeamIdByStudentIdAndDate(
            Long studentId,
            LocalDate targetDate
    );
}