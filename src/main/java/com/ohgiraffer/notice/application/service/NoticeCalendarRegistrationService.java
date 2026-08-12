package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.RegisterNoticeScheduleCommand;
import com.ohgiraffer.notice.application.port.CalendarEventRegistrationPort;
import com.ohgiraffer.notice.application.usecase.NoticeCalendarRegistrationUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 확정된 일정을 캘린더에 등록하고 공지를 등록 완료로 표시한다.
 *
 * <p>둘을 한 트랜잭션에서 처리한다. 일정만 들어가고 표시가 안 되면 화면에 버튼이 남아
 * 같은 일정을 다시 등록하게 되고, 표시만 되고 일정이 안 들어가면 등록할 방법이 사라진다.
 */
@Service
public class NoticeCalendarRegistrationService
        implements NoticeCalendarRegistrationUseCase {

    private final NoticeRepository noticeRepository;
    private final CalendarEventRegistrationPort calendarEventRegistrationPort;

    public NoticeCalendarRegistrationService(
            NoticeRepository noticeRepository,
            CalendarEventRegistrationPort calendarEventRegistrationPort
    ) {
        this.noticeRepository = noticeRepository;
        this.calendarEventRegistrationPort = calendarEventRegistrationPort;
    }

    @Override
    @Transactional
    public int register(
            Long noticeId,
            List<RegisterNoticeScheduleCommand> schedules,
            Long requesterId
    ) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        /*
         * 이미 등록한 공지는 막는다. 화면에서는 버튼이 사라져 있지만, 그 화면을 열어 둔 채로
         * 다른 사람이 먼저 등록했을 수 있다. 통과시키면 같은 일정이 캘린더에 두 번 쌓인다.
         */
        if (notice.isCalendarRegistered()) {
            throw new BusinessException(ErrorCode.NOTICE_CALENDAR_ALREADY_REGISTERED);
        }

        int registered = calendarEventRegistrationPort.register(schedules, requesterId);

        noticeRepository.update(notice.markCalendarRegistered());

        return registered;
    }
}
