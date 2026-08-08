package com.ohgiraffer.todo.application.usecase;

import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  TODO 도메인 조회 UseCase. Controller가 이 인터페이스만 의존
 *  - 요약 엔드포인트에 대응하는 메서드 2개
 */

public interface TodoQueryUseCase {

    // 역할별 도메인별 요약 리스트 (대시보드 카드용)
    List<TodoResponse> getSummaries(Long userId, Role role);

}
