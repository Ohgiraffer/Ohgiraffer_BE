package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;

public interface AttendancePolicyRepository {
    AttendancePolicy save(AttendancePolicy policy);

}
