package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.dto.DailyAttendanceCountView;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;
import com.ohgiraffer.attendance.infrastructure.projection.AttendanceCalendarProjection;
import com.ohgiraffer.attendance.infrastructure.projection.AttendanceSummaryProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataAttendanceRepository extends JpaRepository<AttendanceJpaEntity, Long> {

    @Query("""
    SELECT a.attendanceDate AS attendanceDate, a.status AS status,
           a.checkInTime AS checkInTime, a.checkOutTime AS checkOutTime
    FROM AttendanceJpaEntity a
    WHERE a.userId = :userId
      AND a.attendanceDate BETWEEN :start AND :end
    ORDER BY a.attendanceDate ASC, a.id ASC
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
      AND FUNCTION('WEEKDAY', a.attendanceDate) < 5
    """)
    AttendanceSummaryProjection countByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
    SELECT new com.ohgiraffer.attendance.domain.dto.DailyAttendanceCountView(
        a.attendanceDate,
        SUM(CASE WHEN a.status <> com.ohgiraffer.attendance.domain.model.AttendanceStatus.ABSENT THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status = com.ohgiraffer.attendance.domain.model.AttendanceStatus.ABSENT THEN 1 ELSE 0 END)
    )
    FROM AttendanceJpaEntity a
    WHERE a.userId IN :userIds
      AND a.attendanceDate BETWEEN :start AND :end
    GROUP BY a.attendanceDate
    ORDER BY a.attendanceDate
    """)
    List<DailyAttendanceCountView> countDailyByUserIdsAndDateRange(
            @Param("userIds") List<Long> userIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    Optional<AttendanceJpaEntity> findByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);

    /*
     * 비관적 락
     * 트랜잭션이 그 행을 읽는 순간부터 DB 레벨에서 물리적으로 잠가버려서 다른 트랜잭션이 그 행을 건드리지 못하게 막음
     * */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AttendanceJpaEntity a WHERE a.userId = :userId AND a.attendanceDate = :date")
    Optional<AttendanceJpaEntity> findByUserIdAndAttendanceDateForUpdate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT COUNT(a) FROM AttendanceJpaEntity a
    WHERE a.userId IN :userIds
      AND a.attendanceDate = :date
      AND a.checkInTime IS NOT NULL
    """)
    long countCheckedInByUserIdsAndDate(
            @Param("userIds") List<Long> userIds,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT new com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView(
        a.userId,
        COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'EARLY_LEAVE' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'OUTING' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'LEAVE' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN a.status = 'SICK' THEN 1 ELSE 0 END), 0)
    )
    FROM AttendanceJpaEntity a
    WHERE a.userId IN :userIds
      AND a.attendanceDate BETWEEN :start AND :end
      AND FUNCTION('WEEKDAY', a.attendanceDate) < 5
    GROUP BY a.userId
    """)
    List<StudentAttendanceCountsView> aggregateByUserIdsAndDateRange(
            @Param("userIds") List<Long> userIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}