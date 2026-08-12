package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.port.ScheduleExtractionPort;
import com.ohgiraffer.notice.application.usecase.NoticeScheduleExtractionUseCase;
import com.ohgiraffer.notice.domain.model.ExtractedSchedule;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 공지에서 일정 후보를 찾는다.
 *
 * <p>읽기만 한다. 후보는 화면으로 넘어가 사람의 손을 거친 뒤 별도 요청으로 등록된다.
 */
@Service
public class NoticeScheduleExtractionService
        implements NoticeScheduleExtractionUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final NoticeRepository noticeRepository;
    private final ScheduleExtractionPort scheduleExtractionPort;

    public NoticeScheduleExtractionService(
            NoticeRepository noticeRepository,
            ScheduleExtractionPort scheduleExtractionPort
    ) {
        this.noticeRepository = noticeRepository;
        this.scheduleExtractionPort = scheduleExtractionPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtractedSchedule> extract(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        return scheduleExtractionPort.extract(
                notice.getTitle(),
                notice.getContent(),
                baseDateOf(notice)
        );
    }

    /**
     * 연도가 적히지 않은 날짜를 해석할 기준일.
     *
     * <p>공지 작성일을 쓴다. "8월 5일" 이라고만 적힌 일정은 작성 시점 근처를 뜻하는 것이
     * 보통이라, 오늘 날짜보다 작성일이 정확하다. 지난 공지를 뒤늦게 열어볼 때 특히 그렇다.
     */
    private LocalDate baseDateOf(Notice notice) {
        Instant createdAt = notice.getCreatedAt();

        if (createdAt == null) {
            return LocalDate.now(KST);
        }

        return createdAt.atZone(KST).toLocalDate();
    }
}
