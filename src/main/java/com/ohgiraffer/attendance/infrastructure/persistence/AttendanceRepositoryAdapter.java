package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.dto.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.dto.DailyAttendanceCountView;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.infrastructure.projection.AttendanceSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date) {
        return springDataAttendanceRepository.findByUserIdAndAttendanceDate(userId, date)
                .map(AttendanceJpaEntity::toDomain);
    }

    @Override
    public void save(Attendance attendance) {
        springDataAttendanceRepository.save(AttendanceJpaEntity.fromDomain(attendance));
    }

    @Override
    public Optional<Attendance> findByUserIdAndDateForUpdate(Long userId, LocalDate date) {
        return springDataAttendanceRepository.findByUserIdAndAttendanceDateForUpdate(userId, date)
                .map(AttendanceJpaEntity::toDomain);
    }
}