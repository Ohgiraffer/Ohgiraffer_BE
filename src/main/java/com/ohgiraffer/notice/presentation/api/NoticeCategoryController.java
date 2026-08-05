package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.notice.application.usecase.NoticeCategoryQueryUseCase;
import com.ohgiraffer.notice.presentation.api.response.NoticeCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공지 카테고리", description = "공지 작성 화면 드롭다운과 목록 탭에서 사용")
@RestController
@RequestMapping("/notice-categories")
public class NoticeCategoryController {

    private final NoticeCategoryQueryUseCase noticeCategoryQueryUseCase;

    public NoticeCategoryController(
            NoticeCategoryQueryUseCase noticeCategoryQueryUseCase
    ) {
        this.noticeCategoryQueryUseCase = noticeCategoryQueryUseCase;
    }

    /**
     * 공지 작성 화면의 카테고리 드롭다운과 목록 화면의 탭에서 사용한다.
     */
    @Operation(
            summary = "공지 카테고리 목록 조회",
            description = """
                    등록 순서대로 반환한다.
                    defaultCategory 가 true 인 항목을 작성 화면에서 기본 선택하면 된다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공. 비어 있을 수 있다")
    @GetMapping
    public ResponseEntity<List<NoticeCategoryResponse>> findAll() {
        List<NoticeCategoryResponse> categories =
                noticeCategoryQueryUseCase.findAll()
                        .stream()
                        .map(NoticeCategoryResponse::from)
                        .toList();

        return ResponseEntity.ok(categories);
    }
}
