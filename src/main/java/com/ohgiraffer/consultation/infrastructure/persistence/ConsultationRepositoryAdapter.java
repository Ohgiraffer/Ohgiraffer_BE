package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConsultationRepositoryAdapter implements ConsultationRepository {

        private final SpringDataConsultationRepository springDataRepository;

        @Override
        public Consultation save(Consultation consultation) {
            ConsultationJpaEntity entity = ConsultationJpaEntity.fromDomain(consultation);
            return springDataRepository.save(entity).toDomain();
        }

        @Override
        public Optional<Consultation> findById(Long consultationId) {
            return springDataRepository.findById(consultationId).map(ConsultationJpaEntity::toDomain);
        }

        @Override
        public List<Consultation> findByRequesterId(Long requesterId) {
            return springDataRepository.findByRequesterIdOrderByScheduledAtDesc(requesterId).stream()
                    .map(ConsultationJpaEntity::toDomain)
                    .toList();
        }

        @Override
        public List<Consultation> findUpcoming(LocalDateTime from) {
            return springDataRepository.findUpcoming(from).stream()
                   .map(ConsultationJpaEntity::toDomain)
                   .toList();
       }

        @Override
        public List<Consultation> findByCounselorIdAndScheduledAtBetween(
                Long counselorId, LocalDateTime from, LocalDateTime to) {
            return springDataRepository.findByCounselorIdAndScheduledAtBetween(counselorId, from, to).stream()
                    .map(ConsultationJpaEntity::toDomain)
                    .toList();
        }

        @Override
        public List<Consultation> findAll() {
            return springDataRepository.findAll().stream()
                    .map(ConsultationJpaEntity::toDomain)
                    .toList();
        }

        @Override
        public boolean existsByCounselorIdAndScheduledAt(Long counselorId, LocalDateTime scheduledAt) {
            return springDataRepository.existsByCounselorIdAndScheduledAt(counselorId, scheduledAt);
        }
}
