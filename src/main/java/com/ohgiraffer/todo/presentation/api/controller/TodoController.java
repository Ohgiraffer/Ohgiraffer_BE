package com.ohgiraffer.todo.presentation.api.controller;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.todo.application.usecase.TodoQueryUseCase;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.todo.presentation.api.response.TodoApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* comment.
 *  TODO 도메인 조회 Controller
 *  - 역할별 요약 (대시보드 카드용)
 *  - 응답 배열/객체 직접 반환 (공통 래퍼 없음 - 기존 프로젝트 컨벤션)
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class TodoController {

    private final TodoQueryUseCase todoQueryUseCase;  // TODO 조회 UseCase

    // 역할별 요약 리스트 조회
    @GetMapping("/todo")
    public List<TodoApiResponse> getSummaries(@AuthenticationPrincipal CustomUserPrincipal principal) {
        List<TodoResponse> summaries = todoQueryUseCase.getSummaries(principal.getId(), principal.getRole());
        return summaries.stream()
                .map(s -> new TodoApiResponse(s.sourceDomain(), s.type(), s.count(), s.nearestDueTime()))
                .toList();
    }


}
