package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;

import java.util.List;

public interface AttendancePeriodRepository {
    List<AttendancePeriod> saveAll(List<AttendancePeriod> periods);
}
