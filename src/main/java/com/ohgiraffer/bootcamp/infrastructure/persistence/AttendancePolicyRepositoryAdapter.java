package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public Optional<AttendancePolicy> findByBootcampId(Long bootcampId) {
        return springDataAttendancePolicyRepository.findByBootcampId(bootcampId)
                .map(AttendancePolicyJpaEntity::toDomain);
    }
}
