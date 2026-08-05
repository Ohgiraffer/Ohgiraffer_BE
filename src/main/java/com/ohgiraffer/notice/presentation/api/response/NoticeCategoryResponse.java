package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.domain.model.NoticeCategory;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공지 카테고리 한 건.
 *
 * <p>공지 작성 화면의 드롭다운은 미리 선택된 항목 없이 시작하므로 기본 카테고리 표시는 내려주지 않는다.
 * 목록 화면의 '전체' 탭도 여기에 포함되지 않는다. 필터를 걸지 않는다는 뜻의 화면 라벨이다.
 */
@Schema(description = "공지 카테고리")
public record NoticeCategoryResponse(

        @Schema(description = "카테고리 식별자", example = "1")
        Long categoryId,

        @Schema(description = "카테고리 이름", example = "수업")
        String name
) {

    public static NoticeCategoryResponse from(NoticeCategory category) {
        return new NoticeCategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
