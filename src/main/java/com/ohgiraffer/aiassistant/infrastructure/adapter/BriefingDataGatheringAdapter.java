package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.aiassistant.application.port.AttendanceRiskPort;
import com.ohgiraffer.aiassistant.application.port.BriefingDataGatheringPort;
import com.ohgiraffer.aiassistant.application.port.CalendarQueryPort;
import com.ohgiraffer.aiassistant.application.port.NotificationQueryPort;
import com.ohgiraffer.aiassistant.domain.model.AttendanceRiskInfo;
import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.todo.application.port.*;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/* comment.
 *  BriefingDataGatheringPort 실구현체
 *  - TODO 6개 Port + CalendarQueryPort + NotificationQueryPort를 전부 직접 주입받아 취합
 *  - AttendanceTodoPort는 STUDENT 포함 전 role 호출 (TodoQueryService의 role 필터링을 거치지 않음)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BriefingDataGatheringAdapter implements BriefingDataGatheringPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SubmissionTodoPort submissionTodoPort;
    private final ApprovalTodoPort approvalTodoPort;
    private final NoticeTodoPort noticeTodoPort;
    private final ConsultationTodoPort consultationTodoPort;
    private final AttendanceRiskPort attendanceRiskPort;
    private final CalendarQueryPort calendarQueryPort;
    private final NotificationQueryPort notificationQueryPort;

    @Override
    public BriefingSourceData gather(Long userId, Role role) {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        LocalDateTime now = nowKst.toLocalDateTime();
        LocalDate today = nowKst.toLocalDate();

        List<TodoItemResponse> todoItems = new ArrayList<>();

        if (role == Role.STUDENT) {
            todoItems.addAll(safeGet(() -> submissionTodoPort.getPendingItems(userId, role), userId, "SUBMISSION"));
        }
        todoItems.addAll(safeGet(() -> approvalTodoPort.getPendingItems(userId, role), userId, "APPROVAL"));
        todoItems.addAll(safeGet(() -> noticeTodoPort.getPendingItems(userId, role), userId, "NOTICE"));
        todoItems.addAll(safeGet(() -> consultationTodoPort.getPendingItems(userId, role), userId, "CONSULTATION"));

        LocalDateTime deadline24h = now.plusHours(24);
        List<TodoItemResponse> oneDayDeadlineItems = todoItems.stream()
                .filter(item -> item.dueOrEventTime() != null
                        && !item.dueOrEventTime().isBefore(now)
                        && item.dueOrEventTime().isBefore(deadline24h))
                .toList();

        // 출결은 별도 Port에서 위험도만 조회 - 정상이면 빈 리스트라 attendanceRiskLevel은 null
        List<AttendanceRiskInfo> riskItems = safeGet(() -> attendanceRiskPort.getRiskItems(userId, role), userId, "ATTENDANCE");
        String attendanceRiskLevel = riskItems.stream()
                .map(AttendanceRiskInfo::riskLevel)
                .findFirst()
                .orElse(null);

        return new BriefingSourceData(
                userId,
                role,
                today,
                todoItems,
                oneDayDeadlineItems,
                attendanceRiskLevel,
                safeGet(() -> notificationQueryPort.getUnreadNotifications(userId), userId, "NOTIFICATION"),
                safeGet(() -> calendarQueryPort.getTodayEvents(userId), userId, "CALENDAR")
        );
    }

    private <T> List<T> safeGet(java.util.function.Supplier<List<T>> supplier, Long userId, String sourceName) {
        try {
            return supplier.get();
        } catch (BusinessException e) {
            log.warn("[Briefing] {} 데이터 조회 실패, 해당 항목 생략 | userId={}, code={}, message={}",
                    sourceName, userId, e.getErrorCode().getCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("[Briefing] {} 데이터 조회 중 예상치 못한 오류 | userId={}", sourceName, userId, e);
            return List.of();
        }
    }

}
