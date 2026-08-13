package com.ohgiraffer.team.infrastructure.adapter;

import com.ohgiraffer.chatbot.application.port.TeamQueryPort;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.usecase.*;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/* comment.
 *  TeamQueryPort 실구현체
 *  - getCurrentTeamList: 내부적으로 getTeamPeriods 먼저 호출해서 "오늘 날짜 포함 기간"을 직접 찾은 뒤
 *    getTeamList를 재호출하는 2단 조합 로직 (Gemini에게 teamPeriodId 판단을 맡기지 않기 위함)
 *  - 오늘 날짜 포함 기간이 없으면(팀 편성 공백기) 빈 리스트 반환
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamQueryAdapter implements TeamQueryPort {

    private final GetTeamListUseCase getTeamListUseCase;
    private final GetTeamPeriodListUseCase getTeamPeriodListUseCase;
    private final GetTeamHistoryUseCase getTeamHistoryUseCase;
    private final GetUnassignedStudentUseCase getUnassignedStudentUseCase;

    @Override
    public List<TeamListResult> getCurrentTeamList(Long userId, Role role) {
        Long currentPeriodId = resolveCurrentTeamPeriodId(userId, role);
        if (currentPeriodId == null) {
            log.info("[ChatbotTeamQuery] 오늘 날짜 포함하는 팀 편성 기간 없음 | userId={}", userId);
            return Collections.emptyList();
        }
        return getTeamListUseCase.getTeams(userId, role, currentPeriodId);
    }

    // 팀 편성 기간 목록 중 오늘 날짜(startDate~endDate)를 포함하는 기간의 ID를 탐색
    private Long resolveCurrentTeamPeriodId(Long userId, Role role) {
        LocalDate today = LocalDate.now();
        List<TeamPeriodResult> periods = getTeamPeriodListUseCase.getTeamPeriods(userId, role);

        return periods.stream()
                .filter(p -> !today.isBefore(p.startDate()) && !today.isAfter(p.endDate()))
                .map(TeamPeriodResult::teamPeriodId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<TeamPeriodResult> getTeamPeriods(Long userId, Role role) {
        return getTeamPeriodListUseCase.getTeamPeriods(userId, role);
    }

    @Override
    public TeamHistoryResult getTeamHistory(Long userId, Role role, Long teamPeriodId, LocalDate startDate, LocalDate endDate) {
        if (teamPeriodId == null) {
            // Gemini가 teamPeriodId 없이 이 함수를 호출하는 경우 방어 - 선행 조회(getTeamPeriods) 누락 상황
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "teamPeriodId가 필요합니다.");
        }
        return getTeamHistoryUseCase.getTeamHistories(userId, role, teamPeriodId, startDate, endDate);
    }

    @Override
    public List<UnassignedStudentResult> getUnassignedStudents(Long userId, Role role) {
        return getUnassignedStudentUseCase.getUnassignedStudents(userId, role);
    }


}
