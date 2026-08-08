package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  전자결재 TODO 조회 Port
 *  - /approvals 기반
 *  - 훈련생=본인 신청 처리중, 매니저=확인/승인 대기로 role별 필터링 조건 달라짐
 *  - dueOrEventTime은 결재에 마감 개념이 없어 항상 null
 */

public interface ApprovalTodoPort {

    // role별 결재 건수 요약
    TodoResponse getSummary(Long userId, Role role);

    // 결재 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
