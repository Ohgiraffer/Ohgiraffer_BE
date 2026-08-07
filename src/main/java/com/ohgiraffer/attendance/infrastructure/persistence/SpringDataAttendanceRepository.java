package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.infrastructure.projection.AttendanceCalendarProjection;
import com.ohgiraffer.attendance.infrastructure.projection.AttendanceSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataAttendanceRepository extends JpaRepository<AttendanceJpaEntity, Long> {

    @Query("""
        SELECT a.attendanceDate AS attendanceDate, a.status AS status
        FROM AttendanceJpaEntity a
        WHERE a.userId = :userId
          AND a.attendanceDate BETWEEN :start AND :end
        """)
    List<AttendanceCalendarProjection> findCalendarByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0)     AS presentDays,
            COALESCE(SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END), 0)        AS lateCount,
            COALESCE(SUM(CASE WHEN a.status = 'EARLY_LEAVE' THEN 1 ELSE 0 END), 0) AS earlyLeaveCount,
            COALESCE(SUM(CASE WHEN a.status = 'OUTING' THEN 1 ELSE 0 END), 0)      AS outingCount,
            COALESCE(SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END), 0)      AS absentDays,
            COALESCE(SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END), 0)       AS leaveDays,
            COALESCE(SUM(CASE WHEN a.status = 'SICK' THEN 1 ELSE 0 END), 0)        AS sickDays
        FROM AttendanceJpaEntity a
        WHERE a.userId = :userId
          AND a.attendanceDate BETWEEN :start AND :end
        """)
    AttendanceSummaryProjection countByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}