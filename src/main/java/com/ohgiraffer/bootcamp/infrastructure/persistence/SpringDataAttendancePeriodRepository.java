package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface SpringDataAttendancePeriodRepository extends JpaRepository<AttendancePeriodJpaEntity, Long> {
    List<AttendancePeriodJpaEntity> findAllByBootcampIdOrderByPeriodNo(Long bootcampId);
    void deleteAllByBootcampId(Long bootcampId);

    List<AttendancePeriodJpaEntity> findAllByPeriodStart(LocalDate periodStart);
}