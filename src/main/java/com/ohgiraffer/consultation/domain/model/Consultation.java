package com.ohgiraffer.consultation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String cancelledBy;
    private String cancelReason;

    @Builder
    private Consultation(Long id, Long counselorId, Long requesterId, String topic, String content,
                         String counselorNote, String aiBrief, ConsultationStatus status,
                         LocalDateTime scheduledAt, String externalRefId, String cancelledBy, String cancelReason) {
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
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
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

    public void approve() {
        validateNotClosed();
        this.status = ConsultationStatus.APPROVED;
    }

    public void completeWithRecord(String counselorNote) {
        if (this.status == ConsultationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_CLOSED);
        }
        this.counselorNote = counselorNote;
        this.status = ConsultationStatus.COMPLETED;
    }

    public void cancel(String cancelledBy, String cancelReason) {
        validateNotClosed();
        this.status = ConsultationStatus.CANCELLED;
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
    }

    private void validateNotClosed() {
        if (this.status == ConsultationStatus.CANCELLED || this.status == ConsultationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CONSULTATION_ALREADY_CLOSED);
        }
    }
}