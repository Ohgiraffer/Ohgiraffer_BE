package com.ohgiraffer.consultation.domain.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface CounselorAvailableTimeRepository {

    List<LocalTime> saveAll(Long availableDateId, List<LocalTime> times);

    List<LocalTime> findByAvailableDateId(Long availableDateId);

    Map<Long, List<LocalTime>> findByAvailableDateIdIn(List<Long> availableDateIds);

    void deleteByAvailableDateId(Long availableDateId);
}