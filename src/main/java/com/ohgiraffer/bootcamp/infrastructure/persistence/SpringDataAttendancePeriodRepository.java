package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAttendancePeriodRepository extends JpaRepository<AttendancePeriodJpaEntity, Long> {
    List<AttendancePeriodJpaEntity> findAllByBootcampIdOrderByPeriodNo(Long bootcampId);
    void deleteAllByBootcampId(Long bootcampId);
}