package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.AttendanceExternalSheetLink;

import java.util.Optional;

public interface AttendanceExternalSheetLinkRepository {

    AttendanceExternalSheetLink save(AttendanceExternalSheetLink attendanceExternalSheetLink);

    Optional<AttendanceExternalSheetLink> findLatest();

    Optional<AttendanceExternalSheetLink> findById(Long attendanceSheetLinkId);
}