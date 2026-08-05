package com.ohgiraffer.notice.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 공지 카테고리 등록 요청. 관리 화면의 "새 카테고리명" 입력칸에 대응한다.
 */
@Schema(description = "공지 카테고리 등록 요청")
public record CreateNoticeCategoryRequest(

        @Schema(description = "카테고리 이름", example = "기타")
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 50자를 넘을 수 없습니다.")
        String name
) {
}
