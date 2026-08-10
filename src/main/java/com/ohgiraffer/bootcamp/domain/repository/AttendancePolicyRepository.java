package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;

import java.util.Optional;

public interface AttendancePolicyRepository {
    AttendancePolicy save(AttendancePolicy policy);
    Optional<AttendancePolicy> findByBootcampId(Long bootcampId);

}
