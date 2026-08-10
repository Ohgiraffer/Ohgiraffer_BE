package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  상담 TODO 조회 Port
 *  - /consultations-list 기반 (ConsultationTodoMockAdapter로 임시 대체)
 *  - 실제 API 완성되면 ConsultationTodoAdapter로 교체 예정
 *  - dueOrEventTime은 상담 예정 시각 필수로 채움 (훈련생 role에서 "시간" 표시용)
 */

public interface ConsultationTodoPort {

    // 상담예정 건수 요약 (훈련생은 서비스 계층에서 시간으로 재가공)
    TodoResponse getSummary(Long userId, Role role);

    // 상담예정 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
