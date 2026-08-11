package com.ohgiraffer.consultation.domain.repository;

import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultationRepository {

    Consultation save(Consultation consultation);

    Optional<Consultation> findById(Long consultationId);

    List<Consultation> findByRequesterId(Long requesterId);

    List<Consultation> findUpcoming(Long counselorId, LocalDateTime from, ConsultationStatus status);

    List<Consultation> findAll();

    boolean existsByCounselorIdAndScheduledAtAndStatusNot(
            Long counselorId, LocalDateTime scheduledAt, ConsultationStatus excludedStatus);

    List<Consultation> findByCounselorIdAndScheduledAtBetweenAndStatusNot(
            Long counselorId, LocalDateTime from, LocalDateTime to, ConsultationStatus excludedStatus);

    List<Consultation> findByStatusAndScheduledAtBefore(
            ConsultationStatus status, LocalDateTime dateTime);
}