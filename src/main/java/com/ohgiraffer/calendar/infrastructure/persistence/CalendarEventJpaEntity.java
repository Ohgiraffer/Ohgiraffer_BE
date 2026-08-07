package com.ohgiraffer.calendar.infrastructure.persistence;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 캘린더 일정.
 *
 * <p>테이블에 google_event_id 와 team_id 컬럼이 있으나 매핑하지 않는다.
 * 구글 캘린더 연동은 아직 없고, 반별 캘린더도 쓰지 않기로 정했다. ERD 정리 대상이다.
 */
@Entity
@Table(name = "calendar_event")
public class CalendarEventJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_event_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "is_all_day", nullable = false)
    private boolean allDay;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "is_auto_registered", nullable = false)
    private boolean autoRegistered;

    @Column(name = "ai_extracted", nullable = false)
    private boolean aiExtracted;

    protected CalendarEventJpaEntity() {
    }

    private CalendarEventJpaEntity(CalendarEvent event) {
        this.title = event.getTitle();
        this.eventType = event.getEventType().name();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.allDay = event.isAllDay();
        this.location = event.getLocation();
        this.createdBy = event.getCreatedBy();
        this.autoRegistered = event.isAutoRegistered();
        this.aiExtracted = event.isAiExtracted();
    }

    public static CalendarEventJpaEntity from(CalendarEvent event) {
        return new CalendarEventJpaEntity(event);
    }

    public CalendarEvent toDomain() {
        return CalendarEvent.restore(
                id,
                title,
                EventType.from(eventType),
                startTime,
                endTime,
                allDay,
                location,
                createdBy,
                autoRegistered,
                aiExtracted
        );
    }
}
