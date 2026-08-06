package com.ohgiraffer.survey.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDateTime;

public final class SurveyForm {

    private final Long id;
    private final String title;
    private final LocalDateTime dueAt;
    private final SurveyFormStatus status;
    private final String googleFormId;
    private final Long createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SurveyForm(
            Long id,
            String title,
            LocalDateTime dueAt,
            SurveyFormStatus status,
            String googleFormId,
            Long createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.dueAt = dueAt;
        this.status = status;
        this.googleFormId = googleFormId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /*
     * 새로운 설문 폼을 생성할 때 사용합니다.
     *
     * Google Forms API 호출이 성공하여 googleFormId를 받은 후
     * Application Service가 이 메서드를 호출합니다.
     */
    public static SurveyForm create(
            String title,
            LocalDateTime dueAt,
            String googleFormId,
            Long createdBy
    ) {
        validateTitle(title);
        validateDueAt(dueAt);
        validateGoogleFormId(googleFormId);
        validateCreatedBy(createdBy);

        return new SurveyForm(
                null,
                title.trim(),
                dueAt,
                SurveyFormStatus.DRAFT,
                googleFormId.trim(),
                createdBy,
                null,
                null
        );
    }

    /*
     * DB에 저장된 데이터를 다시 도메인 객체로 복원할 때 사용합니다.
     *
     * infrastructure.persistence의 Repository Adapter에서
     * JPA Entity를 SurveyForm으로 변환할 때 호출합니다.
     */
    public static SurveyForm restore(
            Long id,
            String title,
            LocalDateTime dueAt,
            SurveyFormStatus status,
            String googleFormId,
            Long createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }

        validateTitle(title);
        validateDueAt(dueAt);
        validateStatus(status);
        validateGoogleFormId(googleFormId);
        validateCreatedBy(createdBy);

        return new SurveyForm(
                id,
                title.trim(),
                dueAt,
                status,
                googleFormId.trim(),
                createdBy,
                createdAt,
                updatedAt
        );
    }

    private static void validateTitle(
            String title
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 필수입니다."
            );
        }

        if (title.trim().length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 255자 이하로 입력해야 합니다."
            );
        }
    }

    private static void validateDueAt(
            LocalDateTime dueAt
    ) {
        if (dueAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 응답 마감 일시는 필수입니다."
            );
        }
    }

    private static void validateStatus(
            SurveyFormStatus status
    ) {
        if (status == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 상태는 필수입니다."
            );
        }
    }

    private static void validateGoogleFormId(
            String googleFormId
    ) {
        if (googleFormId == null
                || googleFormId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Form ID는 필수입니다."
            );
        }

        if (googleFormId.trim().length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Form ID는 255자 이하이어야 합니다."
            );
        }
    }

    private static void validateCreatedBy(
            Long createdBy
    ) {
        if (createdBy == null || createdBy <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 생성자 ID가 올바르지 않습니다."
            );
        }
    }

    public SurveyForm update(String title, LocalDateTime dueAt, SurveyFormStatus newStatus, LocalDateTime now
    ) {
        validateTitle(title);
        validateDueAt(dueAt);
        validateStatus(newStatus);

        if (now == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "현재 시간은 필수입니다."
            );
        }

        validateStatusTransition(newStatus);
        validateUpdatedDueAt(dueAt, newStatus, now);

        return new SurveyForm(
                id,
                title.trim(),
                dueAt,
                newStatus,
                googleFormId,
                createdBy,
                createdAt,
                updatedAt
        );
    }

    private void validateStatusTransition(
            SurveyFormStatus newStatus
    ) {
        if (status == newStatus) {
            return;
        }

        boolean validTransition =
                switch (status) {
                    case DRAFT ->
                            newStatus == SurveyFormStatus.PUBLISHED;

                    case PUBLISHED ->
                            newStatus == SurveyFormStatus.CLOSED;

                    case CLOSED ->
                            false;
                };

        if (!validTransition) {
            throw new BusinessException(
                    ErrorCode.SURVEY_FORM_INVALID_STATUS_TRANSITION,
                    "설문 상태는 DRAFT → PUBLISHED → CLOSED 순서로만 변경할 수 있습니다."
            );
        }
    }

    private void validateUpdatedDueAt(
            LocalDateTime dueAt,
            SurveyFormStatus newStatus,
            LocalDateTime now
    ) {
        /*
         * 종료 상태는 이미 마감일이 지난 설문도 처리할 수 있어야 하므로
         * 미래 시간 검증을 적용하지 않습니다.
         */
        if (newStatus == SurveyFormStatus.CLOSED) {
            return;
        }

        if (!dueAt.isAfter(now)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "응답 마감 일시는 현재 시간 이후여야 합니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public SurveyFormStatus getStatus() {
        return status;
    }

    public String getGoogleFormId() {
        return googleFormId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /*
     * Google Form 편집 화면 URL입니다.
     *
     * URL은 googleFormId로 항상 다시 만들 수 있으므로
     * DB에 중복 저장하지 않습니다.
     */
    public String getEditUrl() {
        return "https://docs.google.com/forms/d/"
                + googleFormId
                + "/edit";
    }
}