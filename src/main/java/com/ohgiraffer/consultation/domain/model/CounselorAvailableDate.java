package com.ohgiraffer.consultation.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorAvailableDate {

    private static final LocalTime SLOT_START = LocalTime.of(9, 0);
    private static final LocalTime SLOT_END = LocalTime.of(19, 0);
    private static final int SLOT_MINUTES = 30;

    private Long id;
    private Long counselorId;
    private LocalDate availableDate;
    private List<LocalTime> times = new ArrayList<>();
    private LocalDateTime createdAt;

    @Builder
    private CounselorAvailableDate(Long id, Long counselorId, LocalDate availableDate,
                                   List<LocalTime> times, LocalDateTime createdAt) {
        this.id = id;
        this.counselorId = counselorId;
        this.availableDate = availableDate;
        this.times = times != null ? new ArrayList<>(times) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static CounselorAvailableDate of(Long counselorId, LocalDate availableDate, List<LocalTime> times) {
        validateTimes(times);
        return CounselorAvailableDate.builder()
                .counselorId(counselorId)
                .availableDate(availableDate)
                .times(times)
                .build();
    }

    public void replaceTimes(List<LocalTime> times) {
        validateTimes(times);
        this.times = new ArrayList<>(times);
    }

    private static void validateTimes(List<LocalTime> times) {
        for (LocalTime time : times) {
            if (time.isBefore(SLOT_START) || time.isAfter(SLOT_END)) {
                throw new IllegalArgumentException("상담 가능 시간 범위(09:00~19:00)를 벗어났습니다: " + time);
            }
            if (ChronoUnit.MINUTES.between(SLOT_START, time) % SLOT_MINUTES != 0) {
                throw new IllegalArgumentException("30분 단위 시간만 등록할 수 있습니다: " + time);
            }
        }
    }
}