package com.ohgiraffer.notice.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.port.ScheduleExtractionPort;
import com.ohgiraffer.notice.domain.model.ExtractedSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 제미나이로 공지에서 일정 후보를 찾는다.
 *
 * <p>팀 공용 {@link GeminiClient} 는 문자열만 돌려주므로, JSON 을 요청하고 여기서 읽는다.
 *
 * <p>모델이 형식을 어길 수 있다는 전제로 읽는다. 지시를 어기고 코드 블록으로 감싸거나,
 * 날짜 자리에 "미정" 같은 글자를 넣는 일이 있다. 한 항목이 깨졌다고 전체를 버리면
 * 나머지 멀쩡한 후보까지 사라지므로, 읽을 수 없는 항목만 건너뛴다.
 */
@Component
public class GeminiScheduleExtractionAdapter implements ScheduleExtractionPort {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiScheduleExtractionAdapter.class);

    /** 한 공지에서 받아들일 후보 수. 모달을 넘겨 가며 확인해야 해서 지나치게 많으면 못 쓴다. */
    private static final int MAX_CANDIDATES = 20;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public GeminiScheduleExtractionAdapter(
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExtractedSchedule> extract(
            String title,
            String content,
            LocalDate baseDate
    ) {
        String answer = geminiClient.generateText(
                ScheduleExtractionPromptBuilder.build(title, content, baseDate));

        return toSchedules(readArray(answer));
    }

    private JsonNode readArray(String answer) {
        try {
            JsonNode root = objectMapper.readTree(stripCodeFence(answer));

            if (!root.isArray()) {
                throw new BusinessException(ErrorCode.NOTICE_SCHEDULE_EXTRACTION_FAILED);
            }

            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("공지 일정 추출 응답을 읽지 못했습니다. 응답={}", answer, exception);
            throw new BusinessException(ErrorCode.NOTICE_SCHEDULE_EXTRACTION_FAILED);
        }
    }

    /**
     * 코드 블록 표시를 붙이지 말라고 일러도 붙여 오는 경우가 있어 걷어낸다.
     */
    private String stripCodeFence(String answer) {
        String trimmed = answer.trim();

        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int start = trimmed.indexOf('\n');
        int end = trimmed.lastIndexOf("```");

        if (start < 0 || end <= start) {
            return trimmed;
        }

        return trimmed.substring(start + 1, end).trim();
    }

    private List<ExtractedSchedule> toSchedules(JsonNode array) {
        List<ExtractedSchedule> schedules = new ArrayList<>();

        for (JsonNode element : array) {
            if (schedules.size() >= MAX_CANDIDATES) {
                log.warn("공지 일정 후보가 {}건을 넘어 이후는 버립니다.", MAX_CANDIDATES);
                break;
            }

            ExtractedSchedule schedule = toSchedule(element);

            if (schedule != null && schedule.isUsable()) {
                schedules.add(schedule);
            }
        }

        return schedules;
    }

    private ExtractedSchedule toSchedule(JsonNode element) {
        if (!element.isObject()) {
            return null;
        }

        LocalDate startDate = toDate(text(element, "startDate"));
        LocalDate endDate = toDate(text(element, "endDate"));

        /*
         * 하루짜리 일정에서 종료일을 빠뜨리는 일이 잦다. 시작일이 있으면 그 값을 쓴다.
         */
        if (endDate == null) {
            endDate = startDate;
        }

        return new ExtractedSchedule(
                text(element, "title"),
                toEventType(text(element, "eventType")),
                startDate,
                toTime(text(element, "startTime")),
                endDate,
                toTime(text(element, "endTime")),
                text(element, "location")
        );
    }

    private String text(JsonNode element, String field) {
        JsonNode value = element.get(field);

        if (value == null || value.isNull() || !value.isTextual()) {
            return null;
        }

        String trimmed = value.asText().trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate toDate(String value) {
        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            log.warn("공지 일정 추출에서 읽을 수 없는 날짜를 받았습니다. 값={}", value);
            return null;
        }
    }

    private LocalTime toTime(String value) {
        if (value == null) {
            return null;
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException exception) {
            log.warn("공지 일정 추출에서 읽을 수 없는 시각을 받았습니다. 값={}", value);
            return null;
        }
    }

    /**
     * 유형은 비어 있어도 된다. 모델이 화면에 없는 값을 골라 오면 비운 것으로 본다.
     * 사람이 화면에서 고르면 되는 값이라, 화면에 없는 값을 넘기는 것보다 낫다.
     *
     * <p>화면 드롭다운은 수업/발표·행사·개인 셋뿐이고, 공지에서 뽑은 일정은 개인 일정일 수 없어
     * 두 가지만 남는다. 프롬프트에도 둘만 알려주지만 모델이 다른 값을 지어낼 수 있어 한 번 더 본다.
     */
    private EventType toEventType(String value) {
        if (value == null) {
            return null;
        }

        try {
            EventType eventType = EventType.from(value);

            if (eventType != EventType.CLASS && eventType != EventType.EVENT) {
                log.warn("공지 일정 추출에서 화면에 없는 유형을 받아 비웁니다. 값={}", value);
                return null;
            }

            return eventType;
        } catch (BusinessException exception) {
            log.warn("공지 일정 추출에서 알 수 없는 유형을 받았습니다. 값={}", value);
            return null;
        }
    }
}
