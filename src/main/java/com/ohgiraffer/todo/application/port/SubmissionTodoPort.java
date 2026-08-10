package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  제출물(발표자료/평가만족도) TODO 조회 Port
 *  - /submission-boxes/{id}/submissions, /survey-forms/{id}/responses 기반
 *  - dueOrEventTime은 /submission-boxes/{id} 상세 조회로 채움
 */

public interface SubmissionTodoPort {

    // 역할별 미제출 건수 요약
    TodoResponse getSummary(Long userId, Role role);

    // 미제출 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
