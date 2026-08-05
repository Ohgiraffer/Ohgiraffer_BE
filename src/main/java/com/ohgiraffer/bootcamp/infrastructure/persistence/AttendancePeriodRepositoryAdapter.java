package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendancePeriodRepositoryAdapter implements AttendancePeriodRepository {

    private final SpringDataAttendancePeriodRepository springDataAttendancePeriodRepository;

    @Override
    public List<AttendancePeriod> saveAll(List<AttendancePeriod> periods) {
        List<AttendancePeriodJpaEntity> entities = periods.stream()
                .map(AttendancePeriodJpaEntity::fromDomain)
                .toList();
        return springDataAttendancePeriodRepository.saveAll(entities).stream()
                .map(AttendancePeriodJpaEntity::toDomain)
                .toList();
    }
    @Override
    public List<AttendancePeriod> findAllByBootcampId(Long bootcampId) {
        return springDataAttendancePeriodRepository.findAllByBootcampId(bootcampId).stream()
                .map(AttendancePeriodJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByBootcampId(Long bootcampId) {
        springDataAttendancePeriodRepository.deleteAllByBootcampId(bootcampId);
        springDataAttendancePeriodRepository.flush();
    }
}