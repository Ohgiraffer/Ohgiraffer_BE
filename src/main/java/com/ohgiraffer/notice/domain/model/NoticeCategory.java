package com.ohgiraffer.notice.domain.model;

/**
 * 공지 카테고리 도메인 모델. JPA와 무관한 순수 객체다.
 *
 * <p>{@code defaultCategory} 는 공지 작성 화면에서 기본 선택되는 카테고리를 뜻한다.
 */
public class NoticeCategory {

    private final Long id;
    private final String name;
    private final boolean defaultCategory;

    private NoticeCategory(
            Long id,
            String name,
            boolean defaultCategory
    ) {
        this.id = id;
        this.name = name;
        this.defaultCategory = defaultCategory;
    }

    public static NoticeCategory restore(
            Long id,
            String name,
            boolean defaultCategory
    ) {
        return new NoticeCategory(id, name, defaultCategory);
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
