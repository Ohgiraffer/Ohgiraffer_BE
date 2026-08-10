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

    List<Consultation> findUpcoming(LocalDateTime from);

    List<Consultation> findByCounselorIdAndScheduledAtBetween(Long counselorId, LocalDateTime from, LocalDateTime to);

    boolean existsByCounselorIdAndScheduledAt(Long counselorId, LocalDateTime scheduledAt);

    List<Consultation> findAll();
}