package com.ohgiraffer.consultation.domain.repository;

import com.ohgiraffer.consultation.domain.model.CounselorAvailableDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CounselorAvailableDateRepository {

    CounselorAvailableDate save(CounselorAvailableDate availableDate);

    Optional<CounselorAvailableDate> findByCounselorIdAndAvailableDate(Long counselorId, LocalDate date);

    Optional<CounselorAvailableDate> findByCounselorIdAndAvailableDateForUpdate(Long counselorId, LocalDate date);

    List<LocalDate> findAvailableDatesOnly(Long counselorId, LocalDate from, LocalDate to);

    Set<Long> findCounselorIdsWithAvailabilityFrom(LocalDate from);
}