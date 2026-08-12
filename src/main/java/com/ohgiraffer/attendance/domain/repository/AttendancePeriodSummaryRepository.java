package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;

import java.util.List;

public interface AttendancePeriodSummaryRepository {
    List<StudentAttendanceCountsView> aggregateByUserIds(List<Long> userIds, List<Long> periodIds);
}
