package com.ohgiraffer.notice.domain.model;

import com.ohgiraffer.calendar.domain.model.EventType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 공지 본문에서 AI 가 찾아낸 일정 후보 하나.
 *
 * <p>후보일 뿐이라 저장하지 않는다. 화면이 이 값을 보여주고, 운영진이 고치거나 빼고 나서
 * 확정한 것만 캘린더로 넘어간다. AI 가 날짜를 잘못 읽어도 사람이 걸러낼 수 있어야 한다.
 *
 * <p>{@code eventType} 은 비어 있을 수 있다. 본문만 보고 수업인지 행사인지 가릴 수 없는
 * 경우가 있어, 확신이 없으면 채우지 않고 사람에게 고르게 한다. 아무 값이나 채워 두면
 * 사람이 그대로 등록해 버려 틀린 유형이 캘린더에 남는다.
 *
 * <p>시각도 비어 있을 수 있다. "8월 8일 오전 중" 처럼 시각 없이 적힌 일정이 흔하다.
 * 그때는 화면이 종일 일정으로 다룬다.
 */
public record ExtractedSchedule(
        String title,
        EventType eventType,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String location
) {

    /**
     * 화면에 내보낼 수 있는 후보인지 여부.
     *
     * <p>일정명과 날짜가 없으면 사용자가 고쳐 쓸 수도 없다. AI 응답에 이런 항목이 섞여 오면
     * 후보 목록에서 빼는 편이 낫다. 빈 칸만 있는 카드를 넘겨보게 할 이유가 없다.
     */
    public boolean isUsable() {
        return title != null
                && !title.isBlank()
                && startDate != null
                && endDate != null
                && !endDate.isBefore(startDate);
    }
}
