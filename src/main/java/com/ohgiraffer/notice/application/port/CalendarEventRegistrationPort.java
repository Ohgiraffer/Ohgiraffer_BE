package com.ohgiraffer.notice.application.port;

import com.ohgiraffer.notice.application.command.RegisterNoticeScheduleCommand;

import java.util.List;

/**
 * 확정된 일정을 캘린더에 넣는다.
 *
 * <p>공지 도메인은 캘린더가 어떻게 저장하는지 알 필요가 없다. 넘기면 들어간다는 것까지만 안다.
 */
public interface CalendarEventRegistrationPort {

    /**
     * @param createdBy 등록한 운영진. 캘린더에서 이 사람이 등록자로 남는다
     * @return 저장된 일정 수
     */
    int register(List<RegisterNoticeScheduleCommand> schedules, Long createdBy);
}
