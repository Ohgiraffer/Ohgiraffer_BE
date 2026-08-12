package com.ohgiraffer.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAttendanceExternalSheetLinkJpaRepository extends JpaRepository<AttendanceExternalSheetLinkJpaEntity, Long> {

    Optional<AttendanceExternalSheetLinkJpaEntity> findFirstByOrderByAttendanceSheetLinkIdDesc();
}