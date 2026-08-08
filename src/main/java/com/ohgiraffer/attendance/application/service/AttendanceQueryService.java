package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceSummaryCache;
import com.ohgiraffer.attendance.application.policy.BootcampAccessPolicy;
import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.domain.model.*;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceBalanceResponse;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
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
    private final BootcampAccessPolicy bootcampAccessPolicy;
    private final AttendanceSummaryCache attendanceSummaryCache;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth) {
        return buildMonthlyAttendance(userId, yearMonth);
    }

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendanceForManager(Long requesterId, Long targetUserId, YearMonth yearMonth) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return buildMonthlyAttendance(targetUserId, yearMonth);
    }

    @Override
    public AttendanceSummaryResponse getSummary(Long userId) {
        return attendanceSummaryCache.getCachedSummary(userId);
    }

    @Override
    public AttendanceSummaryResponse getSummaryForManager(Long requesterId, Long targetUserId) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return attendanceSummaryCache.getCachedSummary(targetUserId);
    }

    @Override
    public AttendanceBalanceResponse getLeaveBalance(Long userId) {
        return buildBalance(userId);
    }

    @Override
    public AttendanceBalanceResponse getLeaveBalanceForManager(Long requesterId, Long targetUserId) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return buildBalance(targetUserId);
    }

    private AttendanceBalanceResponse buildBalance(Long userId) {
        LocalDate today = LocalDate.now();

        int remainingLeave = leaveBalanceRepository.findCurrentByUserId(userId, today)
                .map(LeaveBalance::remainingDays)
                .orElse(0);

        int remainingSick = sickBalanceRepository.findCurrentByUserId(userId, today)
                .map(SickBalance::remainingDays)
                .orElse(0);

        return AttendanceBalanceResponse.of(remainingLeave, remainingSick);
    }

    private MonthlyAttendanceResponse buildMonthlyAttendance(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<AttendanceCalendarView> views =
                attendanceRepository.findCalendarByUserIdAndDateRange(userId, start, end);

        Map<LocalDate, AttendanceCalendarView> viewByDate = views.stream()
                .collect(Collectors.toMap(
                        AttendanceCalendarView::attendanceDate,
                        v -> v,
                        (existing, duplicate) -> {
                            log.warn("[buildMonthlyAttendance] 동일 날짜 출결 중복 발견, 기존 값 유지 | userId={}, date={}",
                                    userId, existing.attendanceDate());
                            return existing;
                        }
                ));

        List<MonthlyAttendanceResponse.DayInfo> days = Stream.iterate(start, d -> d.plusDays(1))
                .limit(end.getDayOfMonth())
                .map(date -> {
                    AttendanceCalendarView view = viewByDate.get(date);
                    return new MonthlyAttendanceResponse.DayInfo(
                            date,
                            view != null ? CalendarStatusGroup.from(view.status()) : null,
                            view != null ? view.checkInTime() : null,
                            view != null ? view.checkOutTime() : null
                    );
                })
                .toList();

        return new MonthlyAttendanceResponse(yearMonth.toString(), days);
    }
}