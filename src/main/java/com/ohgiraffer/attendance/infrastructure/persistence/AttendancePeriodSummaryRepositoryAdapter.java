package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;
import com.ohgiraffer.attendance.domain.repository.AttendancePeriodSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendancePeriodSummaryRepositoryAdapter implements AttendancePeriodSummaryRepository {

    private final SpringDataAttendancePeriodSummaryRepository springDataAttendancePeriodSummaryRepository;

    @Override
    public List<StudentAttendanceCountsView> aggregateByUserIds(List<Long> userIds, List<Long> periodIds) {
        if (userIds.isEmpty() || periodIds.isEmpty()) {
            return List.of();
        }
        return springDataAttendancePeriodSummaryRepository.aggregateByUserIds(userIds, periodIds);
    }
}