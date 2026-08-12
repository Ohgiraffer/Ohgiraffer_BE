package com.ohgiraffer.submission.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StudentTeamRepository {

    /**
     * 기준 일시에 학생이 실제로 소속되어 있던 팀 ID를 조회합니다.
     *
     * 팀 운영 기간뿐만 아니라 팀원의 가입·탈퇴 일시도 함께 확인합니다.
     *
     * @param studentId 학생 ID
     * @param targetAt 팀 소속을 판단할 기준 일시
     * @return 기준 일시에 소속된 팀 ID
     */
    Optional<Long> findTeamIdByStudentIdAndDateTime(
            Long studentId,
            LocalDateTime targetAt
    );
}