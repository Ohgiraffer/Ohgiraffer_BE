package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  평가 TODO 조회 Port
 *  - /evaluations 기반 (EvaluationTodoMockAdapter로 임시 대체)
 *  - 실제 API 완성되면 EvaluationTodoAdapter로 교체 예정
 *  - dueOrEventTime은 평가에 마감 개념이 없어 항상 null
 */

public interface EvaluationTodoPort {

    // 미완료 평가 건수 요약
    TodoResponse getSummary(Long userId, Role role);

    // 미완료 평가 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
