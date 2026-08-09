package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  공지사항 TODO 조회 Port
 *  - /notices 기반, confirmedByMe=false인 것만 대상
 *  - dueOrEventTime은 공지에 마감 개념이 없어 항상 null
 */

public interface NoticeTodoPort {

    // 미확인 공지 건수 요약
    TodoResponse getSummary(Long userId, Role role);

    // 미확인 공지 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
