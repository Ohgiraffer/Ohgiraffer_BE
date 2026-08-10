package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.consultation.application.port.GetUserInfoPort;
import com.ohgiraffer.consultation.application.usecase.ConsultationQueryUsecase;
import com.ohgiraffer.consultation.domain.model.*;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import com.ohgiraffer.consultation.domain.repository.CounselorAvailableDateRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationQueryService implements ConsultationQueryUsecase {

    private final ConsultationRepository consultationRepository;
    private final CounselorAvailableDateRepository availableDateRepository;
    private final GetUserInfoPort getUserInfoPort;

    @Override
    public List<CounselorInfo> getCounselors() {
        Set<Long> counselorIdsWithAvailability =
                availableDateRepository.findCounselorIdsWithAvailabilityFrom(LocalDate.now());

        return getUserInfoPort.getUsersByRole(List.of("INSTRUCTOR", "MANAGER")).stream()
                .filter(u -> counselorIdsWithAvailability.contains(u.userId()))
                .map(u -> new CounselorInfo(u.userId(), u.name(), u.role()))
                .toList();
    }

    @Override
    public Set<LocalDate> getAvailableDates(Long counselorId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return new HashSet<>(availableDateRepository.findAvailableDatesOnly(counselorId, from, to));
    }

    @Override
    public List<AvailableTimeSlot> getAvailableTimes(Long counselorId, LocalDate date) {
        List<LocalTime> registered = getRegisteredTimes(counselorId, date);
        if (registered.isEmpty()) {
            return List.of();
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        Set<LocalTime> booked = consultationRepository
                .findByCounselorIdAndScheduledAtBetween(counselorId, dayStart, dayEnd).stream()
                .filter(c -> c.getStatus() != ConsultationStatus.CANCELLED)
                .map(c -> c.getScheduledAt().toLocalTime())
                .collect(Collectors.toSet());

        return registered.stream()
                .map(t -> new AvailableTimeSlot(t, booked.contains(t)))
                .toList();
    }

    @Override
    public List<LocalTime> getRegisteredTimes(Long counselorId, LocalDate date) {
        return availableDateRepository.findByCounselorIdAndAvailableDate(counselorId, date)
                .map(d -> d.getTimes().stream().sorted().toList())
                .orElseGet(List::of);
    }

    @Override
    public List<ConsultationSummary> getMyConsultations(Long userId) {
        return consultationRepository.findByRequesterId(userId).stream()
                .sorted(Comparator.comparing(Consultation::getScheduledAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> new ConsultationSummary(
                        c.getId(),
                        c.getTopic(),
                        c.getScheduledAt(),
                        resolveName(c.getCounselorId()),
                        c.getStatus()
                ))
                .toList();
    }

    @Override
    public ConsultationDetail getDetail(Long consultationId) {
        Consultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        return new ConsultationDetail(
                c.getId(),
                c.getTopic(),
                resolveName(c.getRequesterId()),
                resolveName(c.getCounselorId()),
                c.getScheduledAt(),
                c.getContent(),
                c.getCounselorNote(),
                c.getStatus()
        );
    }

    @Override
    public List<ConsultationListItem> getUpcoming() {
        return consultationRepository.findUpcoming(LocalDateTime.now()).stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public List<ConsultationListItem> getHistory() {
        return consultationRepository.findAll().stream()
                .sorted(Comparator.comparing(Consultation::getScheduledAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toListItem)
                .toList();
    }

    private ConsultationListItem toListItem(Consultation c) {
        return new ConsultationListItem(
                c.getId(),
                c.getTopic(),
                c.getScheduledAt(),
                resolveName(c.getRequesterId()),
                resolveName(c.getCounselorId()),
                c.getStatus()
        );
    }

    private String resolveName(Long userId) {
        if (userId == null) {
            return null;
        }
        return getUserInfoPort.getUserName(userId);
    }
}