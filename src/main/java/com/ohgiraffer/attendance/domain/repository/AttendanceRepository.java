package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.model.DailyAttendanceCountView;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {
    List<AttendanceCalendarView> findCalendarByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);
    AttendanceSummaryView countByUserAndDateRange(Long userId, LocalDate start, LocalDate end);
    List<DailyAttendanceCountView> countDailyByUserIdsAndDateRange(List<Long> userIds, LocalDate start, LocalDate end);
}
