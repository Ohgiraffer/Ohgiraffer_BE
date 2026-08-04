package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.domain.model.NoticeCategory;

public record NoticeCategoryResponse(
        Long categoryId,
        String name,
        boolean defaultCategory
) {

    public static NoticeCategoryResponse from(NoticeCategory category) {
        return new NoticeCategoryResponse(
                category.getId(),
                category.getName(),
                category.isDefaultCategory()
        );
    }
}
