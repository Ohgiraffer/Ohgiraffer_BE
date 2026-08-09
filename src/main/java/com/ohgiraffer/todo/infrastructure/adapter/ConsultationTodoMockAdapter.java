package com.ohgiraffer.todo.infrastructure.adapter;

import com.ohgiraffer.todo.application.port.ConsultationTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
 *  ConsultationTodoPort Mock 구현체
 *  - /consultations-list API 개발 전 상태라 임시로 고정 응답 반환
 *  - 상담 도메인 실제 API 완성되면 ConsultationTodoAdapter(RestClient 기반)로 교체 예정
 *  - dueOrEventTime을 반드시 채워서 훈련생 role의 "시간" 표시 요구사항을 Mock 단계에서도 검증 가능하게 함
 */

@Slf4j
@Component
public class ConsultationTodoMockAdapter implements ConsultationTodoPort {

    // Mock 요약 - 상담예정 1건 고정 반환
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        LocalDateTime mockDueTime = LocalDateTime.now().plusHours(3);
        return new TodoResponse(TodoSourceDomain.CONSULTATION, "상담 예정", 1L, mockDueTime);
    }

    // Mock 상세 - 상담예정 1건 고정 반환
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        LocalDateTime mockDueTime = LocalDateTime.now().plusHours(3);
        return List.of(
                new TodoItemResponse(TodoSourceDomain.CONSULTATION, 2001L, "상담 예정", "예정", LocalDateTime.now(), mockDueTime)
        );
    }

}
