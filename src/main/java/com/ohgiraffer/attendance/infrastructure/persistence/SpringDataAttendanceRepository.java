package com.ohgiraffer.attendance.infrastructure.persistence;

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
}