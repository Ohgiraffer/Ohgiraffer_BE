package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.notice.domain.model.Notice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 공지 테이블 매핑.
 *
 * <p>테이블에는 confirmation_type 컬럼이 남아 있으나 매핑하지 않는다.
 * 확인 방식을 체크박스로 통일하면서 공지별 확인 유형 구분이 필요 없어졌다.
 * ERD 정리 시 컬럼 삭제 대상이다.
 */
@Entity
@Table(name = "notice")
public class NoticeJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "notice_category_id", nullable = false)
    private Long categoryId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, length = 65535)
    private String content;

    @Column(name = "is_mandatory", nullable = false)
    private boolean pinned;

    @Column(name = "is_visible_to_trainee", nullable = false)
    private boolean visibleToTrainee;

    protected NoticeJpaEntity() {
    }

    private NoticeJpaEntity(
            Long id,
            Long authorId,
            Long categoryId,
            String title,
            String content,
            boolean pinned,
            boolean visibleToTrainee
    ) {
        this.id = id;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.pinned = pinned;
        this.visibleToTrainee = visibleToTrainee;
    }

    public static NoticeJpaEntity from(Notice notice) {
        return new NoticeJpaEntity(
                notice.getId(),
                notice.getAuthorId(),
                notice.getCategoryId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.isVisibleToTrainee()
        );
    }

    /**
     * 영속 상태에서 내용만 바꾼다. 식별자·작성자·생성 시각은 건드리지 않는다.
     * 변경 감지로 UPDATE 되며 updated_at 은 감사 기능이 채운다.
     */
    public void applyUpdate(Notice notice) {
        this.categoryId = notice.getCategoryId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.pinned = notice.isPinned();
        this.visibleToTrainee = notice.isVisibleToTrainee();
    }

    public Notice toDomain() {
        return Notice.restore(
                id,
                authorId,
                categoryId,
                title,
                content,
                pinned,
                visibleToTrainee,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }
}
