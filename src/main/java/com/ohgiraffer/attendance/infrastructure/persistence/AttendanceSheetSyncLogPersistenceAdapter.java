package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.AttendanceSheetSyncLog;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceSheetSyncLogPersistenceAdapter implements AttendanceSheetSyncLogRepository {

    private final SpringDataAttendanceSheetSyncLogJpaRepository jpaRepository;

    @Override
    public AttendanceSheetSyncLog save(AttendanceSheetSyncLog attendanceSheetSyncLog) {
        AttendanceSheetSyncLogJpaEntity entity = AttendanceSheetSyncLogJpaEntity.builder()
                .attendanceSheetLinkId(attendanceSheetSyncLog.getAttendanceSheetLinkId())
                .changedRange(attendanceSheetSyncLog.getChangedRange())
                .diffSummary(attendanceSheetSyncLog.getDiffSummary())
                .executorId(attendanceSheetSyncLog.getExecutorId())
                .executorName(attendanceSheetSyncLog.getExecutorName())
                .result(attendanceSheetSyncLog.getResult())
                .build();

        AttendanceSheetSyncLogJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AttendanceSheetSyncLog> findAll() {
        return jpaRepository.findAllByOrderBySyncedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteBefore(LocalDateTime cutoff) {
        jpaRepository.deleteBySyncedAtBefore(cutoff);
    }

    private AttendanceSheetSyncLog toDomain(AttendanceSheetSyncLogJpaEntity entity) {
        return AttendanceSheetSyncLog.builder()
                .syncLogId(entity.getSyncLogId())
                .attendanceSheetLinkId(entity.getAttendanceSheetLinkId())
                .changedRange(entity.getChangedRange())
                .diffSummary(entity.getDiffSummary())
                .syncedAt(entity.getSyncedAt())
                .executorId(entity.getExecutorId())
                .executorName(entity.getExecutorName())
                .result(entity.getResult())
                .build();
    }
}