package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.CounselorAvailableDate;
import com.ohgiraffer.consultation.domain.repository.CounselorAvailableDateRepository;
import com.ohgiraffer.consultation.domain.repository.CounselorAvailableTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CounselorAvailableDateRepositoryAdapter implements CounselorAvailableDateRepository {

    private final SpringDataCounselorAvailableDateRepository springDataDateRepository;
    private final CounselorAvailableTimeRepository availableTimeRepository;

    @Override
    public CounselorAvailableDate save(CounselorAvailableDate availableDate) {
        CounselorAvailableDateJpaEntity dateEntity;

        if (availableDate.getId() != null) {
            dateEntity = springDataDateRepository.findById(availableDate.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 상담 가능일입니다. id=" + availableDate.getId()));
            availableTimeRepository.deleteByAvailableDateId(dateEntity.getId());
        } else {
            dateEntity = springDataDateRepository.save(CounselorAvailableDateJpaEntity.fromDomain(availableDate));
        }

        List<LocalTime> savedTimes = availableTimeRepository.saveAll(dateEntity.getId(), availableDate.getTimes());

        return dateEntity.toDomain(savedTimes);
    }

    @Override
    public Optional<CounselorAvailableDate> findByCounselorIdAndAvailableDate(Long counselorId, LocalDate date) {
        return springDataDateRepository.findByCounselorIdAndAvailableDate(counselorId, date)
                .map(entity -> entity.toDomain(availableTimeRepository.findByAvailableDateId(entity.getId())));
    }

    @Override
    public Optional<CounselorAvailableDate> findByCounselorIdAndAvailableDateForUpdate(Long counselorId, LocalDate date) {
        return springDataDateRepository.findByCounselorIdAndAvailableDateForUpdate(counselorId, date)
                .map(entity -> entity.toDomain(availableTimeRepository.findByAvailableDateId(entity.getId())));
    }

    @Override
    public List<LocalDate> findAvailableDatesOnly(Long counselorId, LocalDate from, LocalDate to) {
        return springDataDateRepository.findAvailableDatesOnly(counselorId, from, to);
    }

    @Override
    public Set<Long> findCounselorIdsWithAvailabilityFrom(LocalDate from) {
        return new HashSet<>(springDataDateRepository.findDistinctCounselorIdsFrom(from));
    }
}