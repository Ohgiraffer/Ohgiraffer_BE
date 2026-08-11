package com.ohgiraffer.calendar.application.event;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.application.usecase.CalendarEventCommandUseCase;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.consultation.application.port.GetUserInfoPort;
import com.ohgiraffer.consultation.domain.event.ConsultationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultationCalendarEventListener {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Duration SLOT_DURATION = Duration.ofMinutes(30);

    private final CalendarEventCommandUseCase calendarEventCommandUseCase;
    private final GetUserInfoPort getUserInfoPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConsultationRequested(ConsultationRequestedEvent event) {
        Instant startTime = event.scheduledAt().atZone(KST).toInstant();
        Instant endTime = startTime.plus(SLOT_DURATION);

        String requesterName = resolveName(event.requesterId());
        String counselorLabel = resolveCounselorLabel(event.counselorId());

        // 상담사 캘린더용
        createEventSafely(
                "상담 일정 - " + requesterName + " 학생 (" + event.topic() + ")",
                startTime, endTime, event.counselorId(), event.consultationId()
        );

        // 학생 캘린더용
        createEventSafely(
                "상담 일정 - " + counselorLabel + " (" + event.topic() + ")",
                startTime, endTime, event.requesterId(), event.consultationId()
        );
    }

    private void createEventSafely(String title, Instant startTime, Instant endTime, Long ownerId, Long consultationId) {
        try {
            calendarEventCommandUseCase.create(new CreateCalendarEventCommand(
                    title,
                    EventType.PERSONAL,
                    startTime,
                    endTime,
                    false,
                    null,
                    ownerId
            ));
            log.info("[상담 캘린더 일정 등록 성공] consultationId={}, ownerId={}", consultationId, ownerId);
        } catch (Exception e) {
            log.warn("[상담 캘린더 일정 등록 실패] consultationId={}, ownerId={}", consultationId, ownerId, e);
        }
    }

    private String resolveName(Long userId) {
        String name = getUserInfoPort.getUserName(userId);
        return name != null ? name : "알 수 없음";
    }

    private String resolveCounselorLabel(Long counselorId) {
        return getUserInfoPort.getUserSummary(counselorId)
                .map(summary -> summary.name() + " " + roleLabel(summary.role()))
                .orElse("알 수 없음");
    }

    private String roleLabel(String role) {
        return switch (role) {
            case "INSTRUCTOR" -> "강사";
            case "MANAGER" -> "매니저";
            default -> "상담사";
        };
    }
}