package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendancePeriodRepository {
    List<AttendancePeriod> saveAll(List<AttendancePeriod> periods);
    List<AttendancePeriod> findAllByBootcampId(Long bootcampId);
    void deleteAllByBootcampId(Long bootcampId);

    List<AttendancePeriod> findAllByPeriodStart(LocalDate periodStart);
}