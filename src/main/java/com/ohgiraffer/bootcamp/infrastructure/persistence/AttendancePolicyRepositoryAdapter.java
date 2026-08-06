package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendancePolicyRepositoryAdapter implements AttendancePolicyRepository {

    private final SpringDataAttendancePolicyRepository springDataAttendancePolicyRepository;

    @Override
    public AttendancePolicy save(AttendancePolicy policy) {
        AttendancePolicyJpaEntity saved = springDataAttendancePolicyRepository
                .save(AttendancePolicyJpaEntity.fromDomain(policy));
        return saved.toDomain();
    }
}
