package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.domain.model.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.model.CalendarStatusGroup;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceQueryService implements AttendanceQueryUsecase {

    private final AttendanceRepository attendanceRepository;

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<AttendanceCalendarView> views =
                attendanceRepository.findCalendarByUserIdAndDateRange(userId, start, end);

        Map<LocalDate, CalendarStatusGroup> statusByDate = views.stream()
                .collect(Collectors.toMap(
                        AttendanceCalendarView::attendanceDate,
                        v -> CalendarStatusGroup.from(v.status()),
                        (existing, duplicate) -> {
                            log.warn("[getMonthlyAttendance] 동일 날짜 출결 중복 발견, 기존 값 유지 | userId={}, date={}",
                                    userId, existing);
                            return existing;
                        }
                ));

        List<MonthlyAttendanceResponse.DayInfo> days = Stream.iterate(start, d -> d.plusDays(1))
                .limit(end.getDayOfMonth())
                .map(date -> new MonthlyAttendanceResponse.DayInfo(
                        date,
                        statusByDate.get(date)
                ))
                .toList();

        return new MonthlyAttendanceResponse(yearMonth.toString(), days);
    }
}