package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConsultationRecordWriter {

    private final ConsultationRepository consultationRepository;

    @Transactional
    public int writeRecord(Long consultationId, Long callerId, String counselorNote) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (!consultation.isCounseledBy(callerId)) {
            throw new BusinessException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        }

        consultation.completeWithRecord(counselorNote);
        consultationRepository.save(consultation);
        return consultation.getRecordVersion();
    }

    @Transactional
    public void applyAiBrief(Long consultationId, String aiBrief, int expectedVersion) {
        consultationRepository.findById(consultationId)
                .ifPresent(consultation -> {
                    consultation.applyAiBrief(aiBrief, expectedVersion);
                    consultationRepository.save(consultation);
                });
    }

    @Transactional
    public void markAiBriefFailed(Long consultationId, int expectedVersion) {
        consultationRepository.findById(consultationId)
                .ifPresent(consultation -> {
                    consultation.markAiBriefFailed(expectedVersion);
                    consultationRepository.save(consultation);
                });
    }
}