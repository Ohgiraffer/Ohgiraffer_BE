package com.ohgiraffer.survey.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_form")
public class SurveyFormJpaEntity
        extends BaseTimeEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "survey_form_id")
    private Long id;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "due_at",
            nullable = false
    )
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private SurveyFormStatus status;

    @Column(
            name = "google_form_id",
            nullable = false,
            unique = true,
            length = 255
    )
    private String googleFormId;

    /*
     * 현재 사용자 도메인 Entity가 준비되지 않았으므로
     * @ManyToOne 관계 대신 FK 값을 Long으로 매핑합니다.
     *
     * DB의 외래키 제약조건은 그대로 동작합니다.
     */
    @Column(
            name = "created_by",
            nullable = false
    )
    private Long createdBy;

    /*
     * JPA는 기본 생성자를 필요로 합니다.
     */
    protected SurveyFormJpaEntity() {
    }

    private SurveyFormJpaEntity(
            Long id,
            String title,
            LocalDateTime dueAt,
            SurveyFormStatus status,
            String googleFormId,
            Long createdBy
    ) {
        this.id = id;
        this.title = title;
        this.dueAt = dueAt;
        this.status = status;
        this.googleFormId = googleFormId;
        this.createdBy = createdBy;
    }

    /*
     * 순수 도메인 모델을 JPA Entity로 변환합니다.
     */
    public static SurveyFormJpaEntity from(
            SurveyForm surveyForm
    ) {
        return new SurveyFormJpaEntity(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getDueAt(),
                surveyForm.getStatus(),
                surveyForm.getGoogleFormId(),
                surveyForm.getCreatedBy()
        );
    }

    /*
     * JPA Entity를 순수 도메인 모델로 변환합니다.
     */
    public SurveyForm toDomain() {
        return SurveyForm.restore(
                id,
                title,
                dueAt,
                status,
                googleFormId,
                createdBy,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getGoogleFormId() {
        return googleFormId;
    }
}