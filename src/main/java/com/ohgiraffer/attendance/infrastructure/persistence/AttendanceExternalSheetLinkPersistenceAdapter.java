package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.AttendanceExternalSheetLink;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttendanceExternalSheetLinkPersistenceAdapter implements AttendanceExternalSheetLinkRepository {

    private final SpringDataAttendanceExternalSheetLinkJpaRepository jpaRepository;

    @Override
    public AttendanceExternalSheetLink save(AttendanceExternalSheetLink attendanceExternalSheetLink) {
        AttendanceExternalSheetLinkJpaEntity entity = AttendanceExternalSheetLinkJpaEntity.builder()
                .attendanceSheetLinkId(attendanceExternalSheetLink.getAttendanceSheetLinkId())
                .sheetUrl(attendanceExternalSheetLink.getSheetUrl())
                .tabName(attendanceExternalSheetLink.getTabName())
                .dateCellRange(attendanceExternalSheetLink.getDateCellRange())
                .columnMapping(attendanceExternalSheetLink.getColumnMapping())
                .lastSyncedAt(attendanceExternalSheetLink.getLastSyncedAt())
                .build();

        AttendanceExternalSheetLinkJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AttendanceExternalSheetLink> findLatest() {
        return jpaRepository.findFirstByOrderByAttendanceSheetLinkIdDesc().map(this::toDomain);
    }

    @Override
    public Optional<AttendanceExternalSheetLink> findById(Long attendanceSheetLinkId) {
        return jpaRepository.findById(attendanceSheetLinkId).map(this::toDomain);
    }

    private AttendanceExternalSheetLink toDomain(AttendanceExternalSheetLinkJpaEntity entity) {
        return AttendanceExternalSheetLink.builder()
                .attendanceSheetLinkId(entity.getAttendanceSheetLinkId())
                .sheetUrl(entity.getSheetUrl())
                .tabName(entity.getTabName())
                .dateCellRange(entity.getDateCellRange())
                .columnMapping(entity.getColumnMapping())
                .lastSyncedAt(entity.getLastSyncedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}