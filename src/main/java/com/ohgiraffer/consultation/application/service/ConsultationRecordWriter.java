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

    // 메모를 저장하고 COMPLETED 처리 - AI 요약 없이

    @Transactional
    public Consultation writeRecord(Long consultationId, Long callerId, String counselorNote) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (!consultation.isCounseledBy(callerId)) {
            throw new BusinessException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        }

        consultation.completeWithRecord(counselorNote);
        return consultationRepository.save(consultation);
    }

    @Transactional
    public void applyAiBrief(Long consultationId, String aiBrief) {
        consultationRepository.findById(consultationId)
                .ifPresent(consultation -> {
                    consultation.applyAiBrief(aiBrief);
                    consultationRepository.save(consultation);
                });
    }

    @Transactional
    public void markAiBriefFailed(Long consultationId) {
        consultationRepository.findById(consultationId)
                .ifPresent(consultation -> {
                    consultation.markAiBriefFailed();
                    consultationRepository.save(consultation);
                });
    }
}