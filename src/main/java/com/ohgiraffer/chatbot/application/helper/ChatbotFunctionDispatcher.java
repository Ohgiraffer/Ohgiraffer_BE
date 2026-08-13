package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.aiassistant.application.port.CalendarQueryPort;
import com.ohgiraffer.aiassistant.application.port.NotificationQueryPort;
import com.ohgiraffer.chatbot.application.port.ApprovalActionPort;
import com.ohgiraffer.chatbot.application.port.TeamQueryPort;
import com.ohgiraffer.chatbot.domain.model.ChatbotGeminiFunctionCall;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.todo.application.port.*;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/*
 * comment.
 *  Gemini가 요청한 functionCall.name을 실제 Port 메서드 호출로 매핑하는 디스패처
 *  - 존재하지 않는 함수명 요청 시 CHATBOT_UNKNOWN_FUNCTION 방어 (ErrorCode 신규 추가 필요 - 아직 미확인)
 *  - role 필터링은 1차로 ChatbotFunctionCatalog(tools 노출 단계)에서 이미 걸러지지만,
 *    여기서도 방어적으로 한 번 더 확인함 (Gemini가 노출 안 된 함수를 억지로 호출 시도하는 극단적 케이스 대비)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotFunctionDispatcher {

    private final SubmissionTodoPort submissionTodoPort;
    private final ApprovalTodoPort approvalTodoPort;
    private final NoticeTodoPort noticeTodoPort;
    private final EvaluationTodoPort evaluationTodoPort;
    private final ConsultationTodoPort consultationTodoPort;
    private final AttendanceTodoPort attendanceTodoPort;
    private final TeamQueryPort teamQueryPort;
    private final ApprovalActionPort approvalActionPort;
    private final NotificationQueryPort notificationQueryPort;
    private final CalendarQueryPort calendarQueryPort;

    public Object dispatch(ChatbotGeminiFunctionCall call, Long userId, Role role) {
        Map<String, Object> args = call.args();

        return switch (call.name()) {
            case "getSubmissionPendingItems" -> requireStudent(role, () -> submissionTodoPort.getPendingItems(userId, role));
            case "getSubmissionSummary" -> requireStudent(role, () -> submissionTodoPort.getSummary(userId, role));
            case "getEvaluationPendingItems" -> requireStudent(role, () -> evaluationTodoPort.getPendingItems(userId, role));
            case "getEvaluationSummary" -> requireStudent(role, () -> evaluationTodoPort.getSummary(userId, role));
            case "getApprovalPendingItems" -> approvalTodoPort.getPendingItems(userId, role);
            case "getApprovalSummary" -> approvalTodoPort.getSummary(userId, role);
            case "getNoticePendingItems" -> noticeTodoPort.getPendingItems(userId, role);
            case "getNoticeSummary" -> noticeTodoPort.getSummary(userId, role);
            case "getConsultationPendingItems" -> consultationTodoPort.getPendingItems(userId, role);
            case "getConsultationSummary" -> consultationTodoPort.getSummary(userId, role);
            case "getAttendanceRisk" -> attendanceTodoPort.getPendingItems(userId, role);
            case "getAttendanceSummary" -> attendanceTodoPort.getSummary(userId, role);

            case "getCurrentTeamList" -> teamQueryPort.getCurrentTeamList(userId, role);
            case "getTeamPeriods" -> teamQueryPort.getTeamPeriods(userId, role);
            case "getTeamHistory" -> teamQueryPort.getTeamHistory(
                    userId, role,
                    longArg(args, "teamPeriodId"),
                    parseDateOrNull(args.get("startDate")),
                    parseDateOrNull(args.get("endDate"))
            );
            case "getUnassignedStudents" -> teamQueryPort.getUnassignedStudents(userId, role);

            case "approveApproval" -> requireStaff(role, () -> approvalActionPort.approve(userId, role, longArg(args, "approvalId")));
            case "rejectApproval" -> requireStaff(role, () -> approvalActionPort.reject(userId, role, longArg(args, "approvalId"), (String) args.get("reason")));
            case "checkApproval" -> requireStaff(role, () -> approvalActionPort.check(userId, role, longArg(args, "approvalId")));
            case "createLeaveApproval" -> requireStudent(role, () -> approvalActionPort.createLeaveApproval(
                    userId,
                    parseDateOrNull(args.get("startDate")),
                    parseDateOrNull(args.get("endDate"))
            ));

            case "getUnreadNotifications" -> notificationQueryPort.getUnreadNotifications(userId);
            case "getTodayCalendarEvents" -> calendarQueryPort.getTodayEvents(userId);

            default -> {
                log.warn("[ChatbotDispatcher] 알 수 없는 함수 호출 시도 | functionName={}, userId={}", call.name(), userId);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "알 수 없는 함수 호출: " + call.name());
            }
        };
    }

    private <T> T requireStudent(Role role, java.util.function.Supplier<T> action) {
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "훈련생만 사용 가능한 기능입니다.");
        }
        return action.get();
    }

    private <T> T requireStaff(Role role, java.util.function.Supplier<T> action) {
        if (role == Role.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "강사/매니저만 사용 가능한 기능입니다.");
        }
        return action.get();
    }

    private Long longArg(Map<String, Object> args, String key) {
        Object raw = args.get(key);
        if (!(raw instanceof Number number)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, key + " 값이 누락되었거나 숫자 형식이 아닙니다.");
        }
        return number.longValue();
    }

    private LocalDate parseDateOrNull(Object raw) {
        return raw == null ? null : LocalDate.parse((String) raw);
    }

}
