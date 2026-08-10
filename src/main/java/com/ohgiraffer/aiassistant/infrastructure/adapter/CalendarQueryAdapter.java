package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.aiassistant.application.port.CalendarQueryPort;
import com.ohgiraffer.calendar.application.query.CalendarEventView;
import com.ohgiraffer.calendar.application.usecase.CalendarEventQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/* comment.
 *  CalendarQueryPort 실구현체 - 캘린더 도메인의 CalendarEventQueryUseCase 직접 주입받아 위임
 *  findByMonth로 이번 달 전체 조회 후, 오늘 날짜에 걸치는 일정만 필터링
 *  ⚠️ CalendarEventView 실제 필드(시작/종료일 필드명) 미확인 - 필터링 로직은 필드 확인 후 보완 필요
 */

@Component
@RequiredArgsConstructor
public class CalendarQueryAdapter implements CalendarQueryPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final CalendarEventQueryUseCase calendarEventQueryUseCase;

    @Override
    public List<CalendarEventView> getTodayEvents(Long userId) {
        LocalDate today = LocalDate.now(KST);

        List<CalendarEventView> monthEvents = calendarEventQueryUseCase.findByMonth(
                today.getYear(), today.getMonthValue(), userId
        );

        return monthEvents.stream()
                .filter(event -> overlapsToday(event, today))
                .toList();
    }

    // 일정 기간(startTime~endTime)이 오늘 날짜에 걸치는지 KST 기준으로 판단 (지난달 시작해 오늘까지 이어지는 일정도 포함)
    private boolean overlapsToday(CalendarEventView event, LocalDate today) {
        LocalDate startDate = event.startTime().atZone(KST).toLocalDate();
        LocalDate endDate = event.endTime().atZone(KST).toLocalDate();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

}
