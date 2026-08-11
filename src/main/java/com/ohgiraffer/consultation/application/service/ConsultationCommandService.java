package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;
import com.ohgiraffer.consultation.application.usecase.ConsultationCommandUsecase;
import com.ohgiraffer.consultation.domain.event.ConsultationRequestedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
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
public class ConsultationCommandService implements ConsultationCommandUsecase {

    private final ConsultationRepository consultationRepository;
    private final CounselorAvailableDateRepository availableDateRepository;
    private final ConsultationAiBriefGenerator aiBriefGenerator;
    private final ConsultationRecordWriter recordWriter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
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

        Long consultationId;
        try {
            consultationId = consultationRepository.save(consultation).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }

        eventPublisher.publishEvent(new ConsultationRequestedEvent(
                consultationId,
                command.counselorId(),
                command.requesterId(),
                command.topic(),
                command.scheduledAt()
        ));

        return consultationId;
    }

    @Override
    public SaveRecordResult saveRecord(SaveRecordCommand command) {
        recordWriter.writeRecord(command.consultationId(), command.callerId(), command.counselorNote());

        Optional<String> aiBrief = aiBriefGenerator.generate(command.counselorNote());

        if (aiBrief.isPresent()) {
            recordWriter.applyAiBrief(command.consultationId(), aiBrief.get());
            return SaveRecordResult.success();
        }

        recordWriter.markAiBriefFailed(command.consultationId());
        return SaveRecordResult.aiFailed();
    }

    @Override
    @Transactional
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