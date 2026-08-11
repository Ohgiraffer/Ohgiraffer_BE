package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consultation_id")
    private Long id;

    @Column(name = "counselor_id")
    private Long counselorId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "content")
    private String content;

    @Column(name = "counselor_note")
    private String counselorNote;

    @Column(name = "ai_brief")
    private String aiBrief;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsultationStatus status;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "external_ref_id")
    private String externalRefId;

    private ConsultationJpaEntity(Long id, Long counselorId, Long requesterId, String topic, String content,
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

    public static ConsultationJpaEntity fromDomain(Consultation domain) {
        return new ConsultationJpaEntity(
                domain.getId(),
                domain.getCounselorId(),
                domain.getRequesterId(),
                domain.getTopic(),
                domain.getContent(),
                domain.getCounselorNote(),
                domain.getAiBrief(),
                domain.getStatus(),
                domain.getScheduledAt(),
                domain.getExternalRefId()
        );
    }

    public Consultation toDomain() {
        return Consultation.builder()
                .id(id)
                .counselorId(counselorId)
                .requesterId(requesterId)
                .topic(topic)
                .content(content)
                .counselorNote(counselorNote)
                .aiBrief(aiBrief)
                .status(status)
                .scheduledAt(scheduledAt)
                .externalRefId(externalRefId)
                .build();
    }
}