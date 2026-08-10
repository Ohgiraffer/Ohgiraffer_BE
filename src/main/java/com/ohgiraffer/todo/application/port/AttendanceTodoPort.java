package com.ohgiraffer.todo.application.port;

import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

/* comment.
 *  출결 위험도 TODO 조회 Port
 *  - /attendance/summary(본인), /attendance/summary/{userId}(관리자용) 기반
 *  - TODO 도메인과 AI비서 도메인이 공용으로 재사용
 *  - dueOrEventTime은 출결에 마감 개념이 없어 항상 null
 */

public interface AttendanceTodoPort {

    // role별 출결 위험도 요약 (훈련생=본인 상태, 강사/매니저=담당 위험군 인원수)
    TodoResponse getSummary(Long userId, Role role);

    // 출결 위험 상태 상세 리스트
    List<TodoItemResponse> getPendingItems(Long userId, Role role);

}
