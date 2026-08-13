package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/* comment.
 *  BriefingSourceData -> Gemini 프롬프트 문자열 조립
 *  - 4블록 구조(역할정의/입력데이터/작성규칙/출력형식) 고정 템플릿에 데이터만 채워넣음
 *  - role별 프롬프트 분리 없음, role은 힌트 한 줄로만 반영하고 나머지는 입력 데이터 자체가 role별로 이미 다름
 */

@Component
public class BriefingPromptBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);

    public String build(BriefingSourceData data) {
        String todayLabel = data.today().format(DATE_FORMATTER)
                + " " + data.today().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        return """
                [1. 역할 정의]
                당신은 부트캠프 학습관리 시스템 CampFlow의 AI 개인 비서입니다.
                %s에게 오늘의 브리핑을 전달하는 역할을 맡고 있습니다.

                [2. 입력 데이터]
                - 오늘 날짜: %s
                - 사용자 역할: %s
                - 출결 상태: %s
                - 오늘 일정: %s
                - 마감 임박 항목 (24시간 이내): %s
                - 안읽은 알림: %s
                - 그 외 대기 중 항목: %s

                [3. 작성 규칙]
                1. 아래 우선순위로 자연스럽게 문단을 구성하세요:
                   1순위 - 출결 위험(제적위험/경고) 상태 — 있다면 가장 먼저, 부드럽지만 명확하게 언급
                   2순위 - 오늘 일정 및 24시간 이내 마감 항목
                   3순위 - 그 외 대기 중인 항목 (간단히)
                2. 각 주제는 1~2문장의 짧은 문단으로 구분해서 작성하세요. 불릿 리스트(-, *)는 사용하지 마세요.
                3. 핵심 숫자나 상태 키워드는 마크다운 굵게(**단어**)로 강조하세요. 문장 전체를 굵게 하지 마세요.
                4. 모든 문장은 "~요"체로 끝내는 부드러운 존댓말로 작성하세요.
                5. 입력 데이터 중 해당 항목이 없으면 그 문단은 생략하세요.
                6. 특별히 전달할 항목이 하나도 없다면, 브리핑 대신 짧고 담백한 격려 문구 한 줄을 자연스럽게 작성하세요. (매번 표현을 다르게)
                7. 위급하지 않은 상황을 과장하지 말고, 사실을 담백하게 전달하세요.
                8. 전체 분량은 4~6문장 내외로 간결하게 작성하세요.
                9. 안읽은 알림이 있다면 "그 외 대기 중인 항목" 문단에 함께 간단히 언급하세요. 별도의 최우선 순위로 다루지 마세요.
                10. 브리핑 첫 문장 시작 전에 오늘 날짜를 "%s" 형식으로 한 줄 표기하세요.

                [4. 출력 형식]
                브리핑 텍스트만 출력, 부연설명 없이 바로 본문 시작
                """.formatted(
                data.role(),
                todayLabel,
                data.role(),
                formatAttendance(data),
                formatTodayEvents(data),
                formatDeadlineItems(data),
                formatNotifications(data),
                formatGeneralItems(data),
                todayLabel
        );
    }

    // 출결 위험도 - null이면 "정상"으로 표기
    private String formatAttendance(BriefingSourceData data) {
        return data.attendanceRiskLevel() == null ? "정상" : data.attendanceRiskLevel();
    }

    // 24시간 이내 마감 항목 - TodoItemResponse 필드 기준으로 포맷
    private String formatDeadlineItems(BriefingSourceData data) {
        if (data.oneDayDeadlineItems() == null || data.oneDayDeadlineItems().isEmpty()) {
            return "없음";
        }
        return data.oneDayDeadlineItems().stream()
                .map(this::formatTodoItem)
                .collect(Collectors.joining(", "));
    }

    // 그 외 대기 중 항목 - 마감임박 제외한 나머지 todoItems (ATTENDANCE 제외, 이미 출결 항목에서 다룸)
    private String formatGeneralItems(BriefingSourceData data) {
        List<TodoItemResponse> general = data.todoItems().stream()
                .filter(item -> !data.oneDayDeadlineItems().contains(item))
                .toList();

        if (general.isEmpty()) {
            return "없음";
        }
        return general.stream()
                .map(this::formatTodoItem)
                .collect(Collectors.joining(", "));
    }

    private String formatTodoItem(TodoItemResponse item) {
        return item.type() + "(" + item.status() + ")";
    }

    private String formatTodayEvents(BriefingSourceData data) {
        if (data.todayEvents() == null || data.todayEvents().isEmpty()) {
            return "없음";
        }
        return data.todayEvents().stream()
                .map(this::formatEvent)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String formatEvent(com.ohgiraffer.calendar.application.query.CalendarEventView event) {
        if (event.allDay()) {
            return event.title() + "(종일)";
        }
        String time = event.startTime()
                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                .toLocalTime()
                .toString();
        return event.title() + "(" + time + ")";
    }

    // 안읽은 알림 - 제목 기준으로 포맷
    private String formatNotifications(BriefingSourceData data) {
        if (data.notifications() == null || data.notifications().isEmpty()) {
            return "없음";
        }
        return data.notifications().stream()
                .map(NotificationResult::title)
                .collect(Collectors.joining(", "));
    }

}
