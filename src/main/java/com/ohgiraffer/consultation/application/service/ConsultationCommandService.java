package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;
import com.ohgiraffer.consultation.application.usecase.ConsultationCommandUsecase;
import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.model.CounselorAvailableDate;
import com.ohgiraffer.consultation.domain.model.SaveRecordResult;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import com.ohgiraffer.consultation.domain.repository.CounselorAvailableDateRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ConsultationCommandService implements ConsultationCommandUsecase {

    private final ConsultationRepository consultationRepository;
    private final CounselorAvailableDateRepository availableDateRepository;
    private final ConsultationAiBriefGenerator aiBriefGenerator;

    @Override
    public Long requestConsultation(RequestConsultationCommand command) {
        boolean isAvailable = availableDateRepository
                .findByCounselorIdAndAvailableDateForUpdate(command.counselorId(), command.scheduledAt().toLocalDate())
                .map(date -> date.getTimes().contains(command.scheduledAt().toLocalTime()))
                .orElse(false);

        if (!isAvailable) {
            throw new BusinessException(ErrorCode.CONSULTATION_TIME_NOT_AVAILABLE);
        }

        if (consultationRepository.existsByCounselorIdAndScheduledAtAndStatusNot(
                command.counselorId(), command.scheduledAt(), ConsultationStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }

        Consultation consultation = Consultation.request(
                command.counselorId(),
                command.requesterId(),
                command.topic(),
                command.content(),
                command.scheduledAt()
        );

        try {
            return consultationRepository.save(consultation).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }
    }

    @Override
    public SaveRecordResult saveRecord(SaveRecordCommand command) {
        Consultation consultation = consultationRepository.findById(command.consultationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (!consultation.isCounseledBy(command.callerId())) {
            throw new BusinessException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        }

        consultation.completeWithRecord(command.counselorNote());

        Optional<String> aiBrief = aiBriefGenerator.generate(command.counselorNote());
        aiBrief.ifPresent(consultation::applyAiBrief);

        consultationRepository.save(consultation);

        return aiBrief.isPresent() ? SaveRecordResult.success() : SaveRecordResult.aiFailed();
    }

    @Override
    public void registerAvailableTime(RegisterAvailableTimeCommand command) {
        Optional<CounselorAvailableDate> existing = availableDateRepository
                .findByCounselorIdAndAvailableDateForUpdate(command.counselorId(), command.date());

        validateNoBookedTimeRemoved(command);

        CounselorAvailableDate availableDate;
        if (existing.isPresent()) {
            availableDate = existing.get();
            availableDate.replaceTimes(command.times());
        } else {
            availableDate = CounselorAvailableDate.of(command.counselorId(), command.date(), command.times());
        }

        try {
            availableDateRepository.save(availableDate);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CONSULTATION_AVAILABLE_DATE_CONFLICT);
        }
    }

    private void validateNoBookedTimeRemoved(RegisterAvailableTimeCommand command) {
        LocalDateTime dayStart = command.date().atStartOfDay();
        LocalDateTime dayEnd = command.date().plusDays(1).atStartOfDay();

        Set<LocalTime> bookedTimes = consultationRepository
                .findByCounselorIdAndScheduledAtBetweenAndStatusNot(
                        command.counselorId(), dayStart, dayEnd, ConsultationStatus.CANCELLED)
                .stream()
                .map(c -> c.getScheduledAt().toLocalTime())
                .collect(Collectors.toSet());

        boolean removingBookedTime = bookedTimes.stream()
                .anyMatch(bookedTime -> !command.times().contains(bookedTime));

        if (removingBookedTime) {
            throw new BusinessException(ErrorCode.CONSULTATION_TIME_IN_USE);
        }
    }
}