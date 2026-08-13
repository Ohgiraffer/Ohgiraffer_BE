package com.ohgiraffer.consultation.infrastructure.adapter;

import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import com.ohgiraffer.todo.application.port.ConsultationTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/*
 * comment.
 *  ConsultationTodoPort 실구현체
 *  - 날짜 필터링 없음 (프론트에서 오늘=시간/오늘이후=날짜로 자체 표시 처리)
 *  - 훈련생: 본인이 신청한 상담 중 PENDING(예정)만
 *  - 강사/매니저: 본인이 담당인, 지금부터 이후의 예정 상담
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationTodoAdapter implements ConsultationTodoPort {

    private final ConsultationRepository consultationRepository;

    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        List<TodoItemResponse> pendingItems = getPendingItems(userId, role);

        LocalDateTime nearestDueTime = pendingItems.stream()
                .map(TodoItemResponse::dueOrEventTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        return new TodoResponse(TodoSourceDomain.CONSULTATION, "상담 예정", pendingItems.size(), nearestDueTime);
    }

    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        List<Consultation> consultations;

        if (role == Role.STUDENT) {
            consultations = consultationRepository.findByRequesterId(userId).stream()
                    .filter(c -> c.getStatus() == ConsultationStatus.PENDING)
                    .toList();
        } else {
            consultations = consultationRepository.findUpcoming(userId, LocalDateTime.now(), ConsultationStatus.PENDING);
        }

        return consultations.stream()
                .map(this::toTodoItemResponse)
                .toList();
    }

    private TodoItemResponse toTodoItemResponse(Consultation consultation) {
        return new TodoItemResponse(
                TodoSourceDomain.CONSULTATION,
                consultation.getId(),
                consultation.getTopic(),
                consultation.getStatus().name(),
                consultation.getScheduledAt(),
                consultation.getScheduledAt()
        );
    }

}
