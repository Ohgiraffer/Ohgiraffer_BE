package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataConsultationRepository extends JpaRepository<ConsultationJpaEntity, Long> {

    List<ConsultationJpaEntity> findByRequesterIdOrderByScheduledAtDesc(Long requesterId);

    boolean existsByCounselorIdAndScheduledAtAndStatusNot(
            Long counselorId, LocalDateTime scheduledAt, ConsultationStatus excludedStatus);

    @Query("""
    SELECT c FROM ConsultationJpaEntity c
    WHERE c.counselorId = :counselorId
      AND c.scheduledAt >= :from
      AND c.scheduledAt < :to
      AND c.status <> :excludedStatus
    """)
    List<ConsultationJpaEntity> findByCounselorIdAndScheduledAtBetweenAndStatusNot(
            @Param("counselorId") Long counselorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludedStatus") ConsultationStatus excludedStatus);

    List<ConsultationJpaEntity> findByStatusAndScheduledAtBefore(
            ConsultationStatus status, LocalDateTime dateTime);

    @Query("""
        SELECT c FROM ConsultationJpaEntity c
        WHERE c.scheduledAt >= :from
          AND c.status = 'PENDING'
        ORDER BY c.scheduledAt ASC
        """)
    List<ConsultationJpaEntity> findUpcoming(@Param("from") LocalDateTime from);
}