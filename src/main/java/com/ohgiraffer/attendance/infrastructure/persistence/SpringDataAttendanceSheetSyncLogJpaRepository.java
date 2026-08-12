package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataAttendanceSheetSyncLogJpaRepository extends JpaRepository<AttendanceSheetSyncLogJpaEntity, Long> {

    List<AttendanceSheetSyncLogJpaEntity> findAllByOrderBySyncedAtDesc();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM AttendanceSheetSyncLogJpaEntity l WHERE l.syncedAt < :cutoff")
    void deleteBySyncedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}