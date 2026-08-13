package com.ohgiraffer.aiassistant.domain.model;

import com.ohgiraffer.calendar.application.query.CalendarEventView;
import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.time.LocalDate;
import java.util.List;

/* comment.
 *  프롬프트 조립에 필요한 원본 데이터를 모아둔 값 객체
 *  - BriefingDataGatheringPort.gather()의 반환 타입
 *  - attendanceRiskLevel은 todoItems 중 sourceDomain=ATTENDANCE 항목에서 추출한 값(없으면 null)
 */

public record BriefingSourceData(
        Long userId,
        Role role,
        LocalDate today,                              // 프롬프트 날짜 표기용, 백엔드에서 계산
        List<TodoItemResponse> todoItems,             // 6개 TodoPort 전체 취합 결과
        List<TodoItemResponse> oneDayDeadlineItems,   // todoItems 중 dueOrEventTime이 24시간 이내인 것
        List<AttendanceRiskInfo> attendanceRiskItems, // AttendanceRiskPort에서 별도로 가져온 값
        List<NotificationResult> notifications,       // 안읽음 알림 목록
        List<CalendarEventView> todayEvents           // 오늘 캘린더 일정
) {
}
