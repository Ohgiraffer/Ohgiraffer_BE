package com.ohgiraffer.todo.infrastructure.adapter;

import com.ohgiraffer.todo.application.port.EvaluationTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
 *  EvaluationTodoPort Mock 구현체
 *  - /evaluations API 개발 전 상태라 임시로 고정 응답 반환
 *  - 평가 도메인 실제 API 완성되면 EvaluationTodoAdapter(RestClient 기반)로 교체 예정
 */

@Slf4j
@Component
public class EvaluationTodoMockAdapter implements EvaluationTodoPort {

    // Mock 요약 - 미완료 평가 2건 고정 반환
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        return new TodoResponse(TodoSourceDomain.EVALUATION, "평가 수정사항", 2L, null);
    }

    // Mock 상세 - 미완료 평가 2건 고정 반환
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        return List.of(
                new TodoItemResponse(TodoSourceDomain.EVALUATION, 1001L, "평가 수정사항", "미확인", LocalDateTime.now(), null),
                new TodoItemResponse(TodoSourceDomain.EVALUATION, 1002L, "평가 수정사항", "미확인", LocalDateTime.now(), null)
        );
    }

}
