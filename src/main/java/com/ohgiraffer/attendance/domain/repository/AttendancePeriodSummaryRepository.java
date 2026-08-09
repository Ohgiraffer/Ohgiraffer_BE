package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.StudentAttendanceCountsView;

import java.util.List;

public interface AttendancePeriodSummaryRepository {
    List<StudentAttendanceCountsView> aggregateByUserIds(List<Long> userIds, List<Long> periodIds);
}
