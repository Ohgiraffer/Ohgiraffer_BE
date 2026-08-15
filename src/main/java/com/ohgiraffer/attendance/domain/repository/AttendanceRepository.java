package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.dto.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.dto.DailyAttendanceCountView;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    List<AttendanceCalendarView> findCalendarByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);
    AttendanceSummaryView countByUserAndDateRange(Long userId, LocalDate start, LocalDate end);
    List<DailyAttendanceCountView> countDailyByUserIdsAndDateRange(List<Long> userIds, LocalDate start, LocalDate end);

    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
    void save(Attendance attendance);

    Optional<Attendance> findByUserIdAndDateForUpdate(Long userId, LocalDate date);

    long countCheckedInByUserIdsAndDate(List<Long> userIds, LocalDate date);

    List<StudentAttendanceCountsView> aggregateByUserIdsAndDateRange(List<Long> userIds, LocalDate start, LocalDate end);
}