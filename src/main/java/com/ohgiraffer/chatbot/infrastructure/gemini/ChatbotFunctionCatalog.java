package com.ohgiraffer.chatbot.infrastructure.gemini;

import com.ohgiraffer.chatbot.domain.model.ChatbotFunctionDefinition;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * comment.
 *  챗봇이 노출 가능한 전체 Function Declaration 카탈로그
 *  - 조회 계열(TodoPort 6종+알림+캘린더+팀) + 액션 계열(결재 승인/반려/확인/휴가신청)
 *  - buildToolsForRole()이 role별로 필터링된 tools 배열(Gemini 요청 규격)을 만들어줌
 *  - userId/role은 어떤 함수에도 파라미터로 노출하지 않음 (서버가 자동 주입, 보안 원칙)
 */

@Component
public class ChatbotFunctionCatalog {

    private static final Set<Role> ALL_ROLES = Set.of(Role.STUDENT, Role.INSTRUCTOR, Role.MANAGER);
    private static final Set<Role> STUDENT_ONLY = Set.of(Role.STUDENT);
    private static final Set<Role> STAFF_ONLY = Set.of(Role.INSTRUCTOR, Role.MANAGER);

    private final List<ChatbotFunctionDefinition> definitions = List.of(

            // ===== 조회 - TODO 계열 =====
            new ChatbotFunctionDefinition("getSubmissionPendingItems", "훈련생 본인의 미제출 제출물(발표자료/평가만족도) 상세 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), STUDENT_ONLY),
            new ChatbotFunctionDefinition("getSubmissionSummary", "훈련생 본인의 미제출 제출물 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), STUDENT_ONLY),
            new ChatbotFunctionDefinition("getEvaluationPendingItems", "훈련생 본인의 미완료 평가 상세 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), STUDENT_ONLY),
            new ChatbotFunctionDefinition("getEvaluationSummary", "훈련생 본인의 미완료 평가 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), STUDENT_ONLY),
            new ChatbotFunctionDefinition("getApprovalPendingItems", "결재 대기 상세 목록을 조회한다. 훈련생은 본인이 신청해 처리중인 결재, 강사/매니저는 본인이 처리해야 할 결재 목록을 반환한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getApprovalSummary", "결재 대기 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getNoticePendingItems", "아직 확인하지 않은 공지사항 상세 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getNoticeSummary", "미확인 공지사항 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getConsultationPendingItems", "예정된 상담 상세 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getConsultationSummary", "예정된 상담 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getAttendanceRisk", "출결 위험 상태 상세 목록을 조회한다. 훈련생은 본인 상태, 강사/매니저는 담당 위험군 훈련생 목록을 반환한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getAttendanceSummary", "출결 위험 건수 요약을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getUnreadNotifications", "읽지 않은 알림 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getTodayCalendarEvents", "오늘 일정(캘린더 이벤트) 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),

            // ===== 조회 - 팀 계열 =====
            new ChatbotFunctionDefinition("getCurrentTeamList", "현재(오늘 날짜 기준) 팀 편성 목록과 각 팀 멤버를 조회한다. '우리 팀 누구야' 같은 질문에 사용한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getTeamPeriods", "전체 팀 편성 기간 목록을 조회한다. 과거 특정 시점의 팀 이력을 조회하기 전에 먼저 호출해서 teamPeriodId를 확보해야 한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),
            new ChatbotFunctionDefinition("getTeamHistory", "특정 팀 편성 기간의 팀 스냅샷과 팀 변경 이력을 조회한다. teamPeriodId는 getTeamPeriods로 먼저 확보해야 한다.",
                    Map.of("type", "OBJECT", "properties", Map.of(
                            "teamPeriodId", Map.of("type", "INTEGER", "description", "조회할 팀 편성 기간 ID (getTeamPeriods 결과에서 확보)"),
                            "startDate", Map.of("type", "STRING", "description", "조회 시작일 (YYYY-MM-DD)"),
                            "endDate", Map.of("type", "STRING", "description", "조회 종료일 (YYYY-MM-DD)")
                    ), "required", List.of("teamPeriodId")), ALL_ROLES),
            new ChatbotFunctionDefinition("getUnassignedStudents", "현재 어떤 팀에도 배정되지 않은 훈련생 목록을 조회한다.", ChatbotFunctionDefinition.noParams(), ALL_ROLES),

            // ===== 액션 - 결재 계열 =====
            new ChatbotFunctionDefinition("approveApproval", "결재 신청을 승인 처리한다.",
                    Map.of("type", "OBJECT", "properties", Map.of(
                            "approvalId", Map.of("type", "INTEGER", "description", "승인할 결재 건의 ID (getApprovalPendingItems 결과에서 확보)")
                    ), "required", List.of("approvalId")), STAFF_ONLY),
            new ChatbotFunctionDefinition("rejectApproval", "결재 신청을 반려 처리한다. 반려 사유가 필수다.",
                    Map.of("type", "OBJECT", "properties", Map.of(
                            "approvalId", Map.of("type", "INTEGER", "description", "반려할 결재 건의 ID"),
                            "reason", Map.of("type", "STRING", "description", "반려 사유")
                    ), "required", List.of("approvalId", "reason")), STAFF_ONLY),
            new ChatbotFunctionDefinition("checkApproval", "결재 신청을 확인 처리한다.",
                    Map.of("type", "OBJECT", "properties", Map.of(
                            "approvalId", Map.of("type", "INTEGER", "description", "확인 처리할 결재 건의 ID")
                    ), "required", List.of("approvalId")), STAFF_ONLY),
            new ChatbotFunctionDefinition("createLeaveApproval", "훈련생 본인의 휴가(연차)를 신청한다. 전자서명은 서버에 등록된 것을 자동으로 사용한다.",
                    Map.of("type", "OBJECT", "properties", Map.of(
                            "startDate", Map.of("type", "STRING", "description", "휴가 시작일 (YYYY-MM-DD)"),
                            "endDate", Map.of("type", "STRING", "description", "휴가 종료일 (YYYY-MM-DD)")
                    ), "required", List.of("startDate", "endDate")), STUDENT_ONLY)
    );

    // role에 맞는 함수만 필터링해서 Gemini 요청용 tools 배열로 변환
    public List<Map<String, Object>> buildToolsForRole(Role role) {
        List<Map<String, Object>> functionDeclarations = definitions.stream()
                .filter(def -> def.allowedRoles().contains(role))
                .map(def -> Map.<String, Object>of(
                        "name", def.name(),
                        "description", def.description(),
                        "parameters", def.parameterSchema()
                ))
                .collect(Collectors.toList());

        return List.of(Map.of("functionDeclarations", functionDeclarations));
    }

}

