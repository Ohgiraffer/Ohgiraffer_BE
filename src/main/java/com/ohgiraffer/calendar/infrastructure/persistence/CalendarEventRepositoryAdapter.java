package com.ohgiraffer.calendar.infrastructure.persistence;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.calendar.domain.repository.CalendarEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class CalendarEventRepositoryAdapter implements CalendarEventRepository {

    private final SpringDataCalendarEventRepository springDataCalendarEventRepository;

    public CalendarEventRepositoryAdapter(
            SpringDataCalendarEventRepository springDataCalendarEventRepository
    ) {
        this.springDataCalendarEventRepository = springDataCalendarEventRepository;
    }

    @Override
    public CalendarEvent save(CalendarEvent calendarEvent) {
        return springDataCalendarEventRepository
                .save(CalendarEventJpaEntity.from(calendarEvent))
                .toDomain();
    }

    @Override
    public List<CalendarEvent> findVisibleInPeriod(
            Instant from,
            Instant to,
            Long userId
    ) {
        return springDataCalendarEventRepository
                .findVisibleInPeriod(
                        from,
                        to,
                        userId,
                        EventType.PERSONAL.name()
                )
                .stream()
                .map(CalendarEventJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<CalendarEvent> findById(Long calendarEventId) {
        return springDataCalendarEventRepository.findById(calendarEventId)
                .map(CalendarEventJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long calendarEventId) {
        springDataCalendarEventRepository.deleteById(calendarEventId);
    }
}
