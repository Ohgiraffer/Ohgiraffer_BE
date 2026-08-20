package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.repository.CounselorAvailableTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CounselorAvailableTimeRepositoryAdapter implements CounselorAvailableTimeRepository {

    private final SpringDataCounselorAvailableTimeRepository springDataRepository;

    @Override
    public List<LocalTime> saveAll(Long availableDateId, List<LocalTime> times) {
        List<CounselorAvailableTimeJpaEntity> entities = times.stream()
                .map(t -> CounselorAvailableTimeJpaEntity.of(availableDateId, t))
                .toList();

        return springDataRepository.saveAll(entities).stream()
                .map(CounselorAvailableTimeJpaEntity::getStartTime)
                .toList();
    }

    @Override
    public List<LocalTime> findByAvailableDateId(Long availableDateId) {
        return springDataRepository.findByAvailableDateId(availableDateId).stream()
                .map(CounselorAvailableTimeJpaEntity::getStartTime)
                .sorted()
                .toList();
    }

    @Override
    public Map<Long, List<LocalTime>> findByAvailableDateIdIn(List<Long> availableDateIds) {
        return springDataRepository.findByAvailableDateIdIn(availableDateIds).stream()
                .collect(Collectors.groupingBy(
                        CounselorAvailableTimeJpaEntity::getAvailableDateId,
                        Collectors.mapping(CounselorAvailableTimeJpaEntity::getStartTime, Collectors.toList())
                ));
    }

    @Override
    public void deleteByAvailableDateId(Long availableDateId) {
        springDataRepository.deleteByAvailableDateId(availableDateId);
    }
}