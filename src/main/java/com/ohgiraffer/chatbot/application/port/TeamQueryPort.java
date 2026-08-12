package com.ohgiraffer.chatbot.application.port;

import com.ohgiraffer.team.application.usecase.TeamHistoryResult;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.TeamPeriodResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.user.domain.model.Role;

import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  AI비서(챗봇) 도메인이 정의하는 팀 조회 포트
 *  - getCurrentTeamList: teamPeriodId를 Gemini에 노출하지 않고 서버가 자동으로 "오늘 포함 기간"을 판단(하이브리드 설계)
 *  - getTeamPeriods/getTeamHistory: 과거 기간 조회 등 명시적 teamPeriodId가 필요한 경우에만 사용
 */

public interface TeamQueryPort {

    // 오늘 날짜가 포함된 팀 편성 기간을 서버가 자동 판단해서 팀 목록 반환 (Gemini 왕복 절약용)
    List<TeamListResult> getCurrentTeamList(Long userId, Role role);

    // 전체 팀 편성 기간 목록 - 과거 기간 조회의 선행 단계로 사용
    List<TeamPeriodResult> getTeamPeriods(Long userId, Role role);

    // 특정 기간의 팀 이력(스냅샷 + 변경이력) 조회
    TeamHistoryResult getTeamHistory(Long userId, Role role, Long teamPeriodId, LocalDate startDate, LocalDate endDate);

    // 미배정 훈련생 목록
    List<UnassignedStudentResult> getUnassignedStudents(Long userId, Role role);


}
