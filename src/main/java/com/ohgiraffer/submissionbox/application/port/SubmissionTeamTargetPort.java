package com.ohgiraffer.submissionbox.application.port;

import java.time.LocalDate;
import java.util.List;

public interface SubmissionTeamTargetPort {

    /**
     * 기준 날짜가 포함된 팀 운영 기간에 소속된 팀을 조회합니다.
     *
     * @param targetDate 제출함 시작일
     * @return 해당 팀 기간에 소속된 팀 목록
     */
    List<TeamSubmissionTarget> findTeamsByTargetDate(
            LocalDate targetDate
    );
}