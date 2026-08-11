package com.ohgiraffer.consultation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consultation {

    private Long id;
    private Long counselorId;
    private Long requesterId;
    private String topic;
    private String content;
    private String counselorNote;
    private String aiBrief;
    private ConsultationStatus status;
    private LocalDateTime scheduledAt;
    private String externalRefId;

    private static final Duration RECORD_DEADLINE = Duration.ofDays(1);

    @Builder
    private Consultation(Long id, Long counselorId, Long requesterId, String topic, String content,
                         String counselorNote, String aiBrief, ConsultationStatus status,
                         LocalDateTime scheduledAt, String externalRefId) {
        this.id = id;
        this.counselorId = counselorId;
        this.requesterId = requesterId;
        this.topic = topic;
        this.content = content;
        this.counselorNote = counselorNote;
        this.aiBrief = aiBrief;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.externalRefId = externalRefId;
    }

    public static Consultation request(Long counselorId, Long requesterId, String topic,
                                       String content, LocalDateTime scheduledAt) {
        return Consultation.builder()
                .counselorId(counselorId)
                .requesterId(requesterId)
                .topic(topic)
                .content(content)
                .status(ConsultationStatus.PENDING)
                .scheduledAt(scheduledAt)
                .build();
    }

    public void completeWithRecord(String counselorNote) {
        if (this.status != ConsultationStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_CLOSED);
        }
        if (LocalDateTime.now().isAfter(this.scheduledAt.plus(RECORD_DEADLINE))) {
            throw new BusinessException(ErrorCode.CONSULTATION_RECORD_DEADLINE_PASSED);
        }
        this.counselorNote = counselorNote;
        this.status = ConsultationStatus.COMPLETED;
    }

    public void expire() {
        if (this.status != ConsultationStatus.PENDING) {
            return;
        }
        this.status = ConsultationStatus.CANCELLED;
    }

    public boolean isRequestedBy(Long userId) {
        return this.requesterId.equals(userId);
    }

    public boolean isCounseledBy(Long userId) {
        return this.counselorId != null && this.counselorId.equals(userId);
    }
}