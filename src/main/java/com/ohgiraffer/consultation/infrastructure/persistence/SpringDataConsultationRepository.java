package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataConsultationRepository extends JpaRepository<ConsultationJpaEntity, Long> {

    List<ConsultationJpaEntity> findByRequesterIdOrderByScheduledAtDesc(Long requesterId);

    List<ConsultationJpaEntity> findByCounselorIdAndScheduledAtBetween(
            Long counselorId, LocalDateTime from, LocalDateTime to);

    boolean existsByCounselorIdAndScheduledAt(Long counselorId, LocalDateTime scheduledAt);

    @Query("""
        SELECT c FROM ConsultationJpaEntity c
        WHERE c.scheduledAt >= :from
          AND c.status IN ('PENDING', 'CHECKED', 'APPROVED')
        ORDER BY c.scheduledAt ASC
        """)
    List<ConsultationJpaEntity> findUpcoming(@Param("from") LocalDateTime from);
}