package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;
import com.ohgiraffer.consultation.application.usecase.ConsultationCommandUsecase;
import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.model.CounselorAvailableDate;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import com.ohgiraffer.consultation.domain.repository.CounselorAvailableDateRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Long requestConsultation(RequestConsultationCommand command) {
        if (consultationRepository.existsByCounselorIdAndScheduledAt(
                command.counselorId(), command.scheduledAt())) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }

        Consultation consultation = Consultation.request(
                command.counselorId(),
                command.requesterId(),
                command.topic(),
                command.content(),
                command.scheduledAt()
        );

        return consultationRepository.save(consultation).getId();
    }

    @Override
    public void saveRecord(SaveRecordCommand command) {
        Consultation consultation = consultationRepository.findById(command.consultationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        consultation.completeWithRecord(command.counselorNote());
        consultationRepository.save(consultation);
    }

    @Override
    public void registerAvailableTime(RegisterAvailableTimeCommand command) {
        validateNoBookedTimeRemoved(command);

        Optional<CounselorAvailableDate> existing = availableDateRepository
                .findByCounselorIdAndAvailableDate(command.counselorId(), command.date());

        CounselorAvailableDate availableDate;
        if (existing.isPresent()) {
            availableDate = existing.get();
            availableDate.replaceTimes(command.times());
        } else {
            availableDate = CounselorAvailableDate.of(command.counselorId(), command.date(), command.times());
        }

        availableDateRepository.save(availableDate);
    }

    private void validateNoBookedTimeRemoved(RegisterAvailableTimeCommand command) {
        LocalDateTime dayStart = command.date().atStartOfDay();
        LocalDateTime dayEnd = command.date().plusDays(1).atStartOfDay();

        Set<LocalTime> bookedTimes = consultationRepository
                .findByCounselorIdAndScheduledAtBetween(command.counselorId(), dayStart, dayEnd).stream()
                .filter(c -> c.getStatus() != ConsultationStatus.CANCELLED)
                .map(c -> c.getScheduledAt().toLocalTime())
                .collect(Collectors.toSet());

        boolean removingBookedTime = bookedTimes.stream()
                .anyMatch(bookedTime -> !command.times().contains(bookedTime));

        if (removingBookedTime) {
            throw new BusinessException(ErrorCode.CONSULTATION_TIME_IN_USE);
        }
    }
}