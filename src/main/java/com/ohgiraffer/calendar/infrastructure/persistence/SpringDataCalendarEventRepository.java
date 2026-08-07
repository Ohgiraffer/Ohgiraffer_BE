package com.ohgiraffer.calendar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataCalendarEventRepository
        extends JpaRepository<CalendarEventJpaEntity, Long> {

    /**
     * 기간에 걸치는 일정을 시작순으로 반환한다.
     *
     * <p>겹침 판정은 "시작이 기간 끝보다 늦지 않고, 종료가 기간 시작보다 이르지 않다" 이다.
     * 기간 안에 시작하는 것만 찾으면 지난달에 시작해 이번 달까지 이어지는 일정이 빠진다.
     *
     * <p>개인 일정은 등록자에게만 보인다. 공용 일정은 등록자와 무관하게 모두에게 보인다.
     */
    @Query("""
            select e
              from CalendarEventJpaEntity e
             where e.startTime <= :to
               and e.endTime >= :from
               and (e.eventType <> :personalType or e.createdBy = :userId)
             order by e.startTime asc
            """)
    List<CalendarEventJpaEntity> findVisibleInPeriod(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("userId") Long userId,
            @Param("personalType") String personalType
    );
}
