package com.ohgiraffer.consultation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCounselorAvailableTimeRepository extends JpaRepository<CounselorAvailableTimeJpaEntity, Long> {

    List<CounselorAvailableTimeJpaEntity> findByAvailableDateId(Long availableDateId);

    List<CounselorAvailableTimeJpaEntity> findByAvailableDateIdIn(List<Long> availableDateIds);

    void deleteByAvailableDateId(Long availableDateId);
}