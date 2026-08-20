package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataAttendancePeriodSummaryRepository extends JpaRepository<AttendancePeriodSummaryJpaEntity, Long> {

    @Query("""
    SELECT new com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView(
        s.userId,
        COALESCE(SUM(s.presentDays), 0),
        COALESCE(SUM(s.lateCount), 0),
        COALESCE(SUM(s.earlyLeaveCount), 0),
        COALESCE(SUM(s.outingCount), 0),
        COALESCE(SUM(s.absentDays), 0),
        COALESCE(SUM(s.leaveDays), 0),
        COALESCE(SUM(s.sickDays), 0)
    )
    FROM AttendancePeriodSummaryJpaEntity s
    WHERE s.userId IN :userIds
      AND s.periodId IN :periodIds
    GROUP BY s.userId
    """)
    List<StudentAttendanceCountsView> aggregateByUserIds(
            @Param("userIds") List<Long> userIds,
            @Param("periodIds") List<Long> periodIds
    );
}