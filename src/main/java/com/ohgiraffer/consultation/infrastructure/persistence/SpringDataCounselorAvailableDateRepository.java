package com.ohgiraffer.consultation.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataCounselorAvailableDateRepository extends JpaRepository<CounselorAvailableDateJpaEntity, Long> {

    Optional<CounselorAvailableDateJpaEntity> findByCounselorIdAndAvailableDate(Long counselorId, LocalDate availableDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT d FROM CounselorAvailableDateJpaEntity d
            WHERE d.counselorId = :counselorId AND d.availableDate = :date
            """)
    Optional<CounselorAvailableDateJpaEntity> findByCounselorIdAndAvailableDateForUpdate(
            @Param("counselorId") Long counselorId, @Param("date") LocalDate date);

    List<CounselorAvailableDateJpaEntity> findByCounselorIdAndAvailableDateBetween(
            Long counselorId, LocalDate from, LocalDate to);

    @Query("""
            SELECT d.availableDate FROM CounselorAvailableDateJpaEntity d
            WHERE d.counselorId = :counselorId
              AND d.availableDate BETWEEN :from AND :to
            """)
    List<LocalDate> findAvailableDatesOnly(
            @Param("counselorId") Long counselorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT DISTINCT d.counselorId FROM CounselorAvailableDateJpaEntity d
            WHERE d.availableDate >= :from
              AND EXISTS (
                  SELECT 1 FROM CounselorAvailableTimeJpaEntity t
                  WHERE t.availableDateId = d.id
              )
            """)
    List<Long> findDistinctCounselorIdsFrom(@Param("from") LocalDate from);
}