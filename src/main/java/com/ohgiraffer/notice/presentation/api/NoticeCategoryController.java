package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.notice.application.usecase.NoticeCategoryQueryUseCase;
import com.ohgiraffer.notice.presentation.api.response.NoticeCategoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
