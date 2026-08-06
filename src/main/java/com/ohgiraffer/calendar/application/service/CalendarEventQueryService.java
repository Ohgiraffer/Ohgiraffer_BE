package com.ohgiraffer.calendar.application.service;

import com.ohgiraffer.calendar.application.query.CalendarEventView;
import com.ohgiraffer.calendar.application.usecase.CalendarEventQueryUseCase;
import com.ohgiraffer.calendar.domain.repository.CalendarEventRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CalendarEventQueryService implements CalendarEventQueryUseCase {

    /**
     * 달의 경계는 보는 사람의 달력 기준이다. UTC 로 자르면 월초와 월말 하루가 어긋난다.
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 하루의 마지막 순간. 말일에 걸친 일정을 놓치지 않기 위해 종료 경계로 쓴다. */
    private static final LocalTime END_OF_DAY =
            LocalTime.of(23, 59, 59, 999_999_000);

    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventQueryService(
            CalendarEventRepository calendarEventRepository
    ) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @Override
    public List<CalendarEventView> findByMonth(
            int year,
            int month,
            Long userId
    ) {
        YearMonth target = toYearMonth(year, month);

        return calendarEventRepository
                .findVisibleInPeriod(
                        startOf(target),
                        endOf(target),
                        userId
                )
                .stream()
                .map(CalendarEventView::of)
                .toList();
    }

    private static YearMonth toYearMonth(int year, int month) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "조회할 수 있는 연도 범위를 벗어났습니다."
            );
        }

        if (month < 1 || month > 12) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "월은 1부터 12 사이여야 합니다."
            );
        }

        return YearMonth.of(year, month);
    }

    private static Instant startOf(YearMonth target) {
        return target.atDay(1).atStartOfDay(KST).toInstant();
    }

    private static Instant endOf(YearMonth target) {
        return target.atEndOfMonth().atTime(END_OF_DAY).atZone(KST).toInstant();
    }
}
