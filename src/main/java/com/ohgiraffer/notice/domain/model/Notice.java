package com.ohgiraffer.notice.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

/**
 * 공지사항 도메인 모델.
 *
 * <p>JPA와 무관한 순수 객체다. 영속화는 infrastructure 계층의 JpaEntity가 담당한다.
 *
 * <p>확인 방식은 별도 속성으로 두지 않는다. 화면 설계상 필수 공지에만 확인 체크박스가
 * 노출되므로 {@code mandatory} 하나로 판정한다.
 */
public class Notice {

    private static final int TITLE_MAX_LENGTH = 255;

    private final Long id;
    private final Long authorId;
    private final Long categoryId;
    private final String title;
    private final String content;
    private final boolean mandatory;
    private final boolean visibleToTrainee;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Notice(
            Long id,
            Long authorId,
            Long categoryId,
            String title,
            String content,
            boolean mandatory,
            boolean visibleToTrainee,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.visibleToTrainee = visibleToTrainee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 공지를 생성한다. 아직 저장 전이라 식별자와 시각은 비어 있다.
     */
    public static Notice create(
            Long authorId,
            Long categoryId,
            String title,
            String content,
            boolean mandatory,
            boolean visibleToTrainee
    ) {
        validateAuthorId(authorId);
        validateCategoryId(categoryId);
        validateTitle(title);
        validateContent(content);

        return new Notice(
                null,
                authorId,
                categoryId,
                title.trim(),
                content,
                mandatory,
                visibleToTrainee,
                null,
                null
        );
    }

    /**
     * 저장소에서 읽어온 값으로 도메인 모델을 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static Notice restore(
            Long id,
            Long authorId,
            Long categoryId,
            String title,
            String content,
            boolean mandatory,
            boolean visibleToTrainee,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Notice(
                id,
                authorId,
                categoryId,
                title,
                content,
                mandatory,
                visibleToTrainee,
                createdAt,
                updatedAt
        );
    }

    /**
     * 훈련생에게 확인 체크박스를 노출해야 하는지 여부.
     */
    public boolean requiresConfirmation() {
        return mandatory;
    }

    /**
     * 해당 조회자에게 이 공지를 보여줄 수 있는지 여부.
     *
     * <p>훈련생 비공개 공지는 훈련생에게 노출하지 않는다. 운영진은 제한이 없다.
     */
    public boolean isVisibleTo(ViewerRole viewer) {
        if (viewer != ViewerRole.TRAINEE) {
            return true;
        }

        return visibleToTrainee;
    }

    private static void validateAuthorId(Long authorId) {
        if (authorId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "작성자 정보가 필요합니다."
            );
        }
    }

    private static void validateCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공지 카테고리를 선택해주세요."
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공지 제목은 필수입니다."
            );
        }

        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공지 제목은 " + TITLE_MAX_LENGTH + "자 이하로 입력해주세요."
            );
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공지 본문은 필수입니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isVisibleToTrainee() {
        return visibleToTrainee;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
