package com.ohgiraffer.calendar.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

/**
 * 통합 캘린더의 일정. JPA와 무관한 순수 객체다.
 *
 * <p>종일 일정은 {@code allDay} 로 구분한다. 등록 화면에서 시각이 선택 입력이라
 * 시간을 넣지 않은 일정과 자정에 시작하는 일정을 시각만으로는 가릴 수 없기 때문이다.
 *
 * <p>개인 일정은 등록한 사람에게만 보인다. {@code createdBy} 가 그 판단 근거다.
 */
public class CalendarEvent {

    private static final int TITLE_MAX_LENGTH = 255;
    private static final int LOCATION_MAX_LENGTH = 255;

    private final Long id;
    private final String title;
    private final EventType eventType;
    private final Instant startTime;
    private final Instant endTime;
    private final boolean allDay;
    private final String location;
    private final Long createdBy;
    private final boolean autoRegistered;
    private final boolean aiExtracted;

    private CalendarEvent(
            Long id,
            String title,
            EventType eventType,
            Instant startTime,
            Instant endTime,
            boolean allDay,
            String location,
            Long createdBy,
            boolean autoRegistered,
            boolean aiExtracted
    ) {
        this.id = id;
        this.title = title;
        this.eventType = eventType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.allDay = allDay;
        this.location = location;
        this.createdBy = createdBy;
        this.autoRegistered = autoRegistered;
        this.aiExtracted = aiExtracted;
    }

    /**
     * 사용자가 직접 등록하는 일정을 만든다.
     *
     * <p>상담 연동이나 AI 추출로 들어오는 일정은 표시 값이 달라야 하므로 이 경로를 쓰지 않는다.
     */
    public static CalendarEvent create(
            String title,
            EventType eventType,
            Instant startTime,
            Instant endTime,
            boolean allDay,
            String location,
            Long createdBy
    ) {
        validateTitle(title);
        validateEventType(eventType);
        validatePeriod(startTime, endTime);
        validateLocation(location);
        validateCreatedBy(createdBy);

        return new CalendarEvent(
                null,
                title.trim(),
                eventType,
                startTime,
                endTime,
                allDay,
                trimToNull(location),
                createdBy,
                false,
                false
        );
    }

    /**
     * 저장소에서 읽어온 값으로 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static CalendarEvent restore(
            Long id,
            String title,
            EventType eventType,
            Instant startTime,
            Instant endTime,
            boolean allDay,
            String location,
            Long createdBy,
            boolean autoRegistered,
            boolean aiExtracted
    ) {
        return new CalendarEvent(
                id,
                title,
                eventType,
                startTime,
                endTime,
                allDay,
                location,
                createdBy,
                autoRegistered,
                aiExtracted
        );
    }

    /**
     * 이 일정을 해당 사용자에게 보여줄 수 있는지 여부.
     *
     * <p>개인 일정은 등록한 본인에게만 보인다. 다른 사람의 면담이나 휴가가
     * 남의 캘린더에 뜨면 안 되기 때문이다. 공용 일정은 모두에게 보인다.
     */
    public boolean isVisibleTo(Long userId) {
        if (!eventType.isPersonal()) {
            return true;
        }

        return createdBy != null && createdBy.equals(userId);
    }

    /**
     * 요구사항상 일정은 등록자만 삭제할 수 있다.
     */
    public boolean isCreatedBy(Long userId) {
        return createdBy != null && createdBy.equals(userId);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정명은 필수입니다."
            );
        }

        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정명은 " + TITLE_MAX_LENGTH + "자를 넘을 수 없습니다."
            );
        }
    }

    private static void validateEventType(EventType eventType) {
        if (eventType == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정 유형은 필수입니다."
            );
        }

        /*
         * 공휴일은 시스템이 넣는 값이다. 사람이 만들 수 있게 두면
         * 등록자가 있는 가짜 공휴일이 생기고, 화면에서는 진짜와 구분되지 않는다.
         */
        if (eventType.isSystemOnly()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공휴일은 직접 등록할 수 없습니다."
            );
        }
    }

    /**
     * 종료가 시작보다 앞설 수 없다. 같은 시각은 허용한다 —
     * 시각 없이 하루짜리로 등록하면 시작과 종료가 같은 날 0시로 들어온다.
     */
    private static void validatePeriod(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정 시작일과 종료일은 필수입니다."
            );
        }

        if (endTime.isBefore(startTime)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정 종료 시점이 시작 시점보다 앞설 수 없습니다."
            );
        }
    }

    private static void validateLocation(String location) {
        if (location != null && location.trim().length() > LOCATION_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "장소는 " + LOCATION_MAX_LENGTH + "자를 넘을 수 없습니다."
            );
        }
    }

    private static void validateCreatedBy(Long createdBy) {
        if (createdBy == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "등록자 정보가 필요합니다."
            );
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public String getLocation() {
        return location;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public boolean isAutoRegistered() {
        return autoRegistered;
    }

    public boolean isAiExtracted() {
        return aiExtracted;
    }
}
