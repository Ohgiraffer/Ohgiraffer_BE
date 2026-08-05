package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notice_category")
public class NoticeCategoryJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_category_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean defaultCategory;

    protected NoticeCategoryJpaEntity() {
    }

    private NoticeCategoryJpaEntity(String name) {
        this.name = name;
        this.defaultCategory = false;
    }

    /**
     * 새로 저장할 카테고리를 만든다. 식별자는 DB가 채번한다.
     *
     * <p>is_default 는 NOT NULL 이라 값을 넣어야 하는데, 작성 화면이 기본 선택을 쓰지 않기로 해
     * 도메인에서 다루지 않는다. 여기서 false 로 채워 제약만 만족시킨다.
     */
    public static NoticeCategoryJpaEntity from(NoticeCategory category) {
        return new NoticeCategoryJpaEntity(category.getName());
    }

    public NoticeCategory toDomain() {
        return NoticeCategory.restore(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }
}
