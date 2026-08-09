package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.model.DailyAttendanceCountView;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.infrastructure.projection.AttendanceSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceRepositoryAdapter implements AttendanceRepository {

    private final SpringDataAttendanceRepository springDataAttendanceRepository;

    @Override
    public List<AttendanceCalendarView> findCalendarByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
        return springDataAttendanceRepository.findCalendarByUserIdAndDateRange(userId, start, end)
                .stream()
                .map(p -> new AttendanceCalendarView(
                        p.getAttendanceDate(), p.getStatus(), p.getCheckInTime(), p.getCheckOutTime()
                ))
                .toList();
    }

    @Override
    public AttendanceSummaryView countByUserAndDateRange(Long userId, LocalDate start, LocalDate end) {
        AttendanceSummaryProjection p = springDataAttendanceRepository.countByUserAndDateRange(userId, start, end);
        return new AttendanceSummaryView(
                p.getPresentDays(),
                p.getLateCount(),
                p.getEarlyLeaveCount(),
                p.getOutingCount(),
                p.getAbsentDays(),
                p.getLeaveDays(),
                p.getSickDays()
        );
    }

    @Override
    public List<DailyAttendanceCountView> countDailyByUserIdsAndDateRange(List<Long> userIds, LocalDate start, LocalDate end) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return springDataAttendanceRepository.countDailyByUserIdsAndDateRange(userIds, start, end);
    }
}