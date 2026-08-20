package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.command.RegisterNoticeScheduleCommand;

import java.util.List;

public interface NoticeCalendarRegistrationUseCase {

    /**
     * 운영진이 확정한 일정을 캘린더에 등록하고, 이 공지의 AI 일정 등록을 마친 것으로 표시한다.
     *
     * @return 등록한 일정 수
     */
    int register(
            Long noticeId,
            List<RegisterNoticeScheduleCommand> schedules,
            Long requesterId
    );
}
