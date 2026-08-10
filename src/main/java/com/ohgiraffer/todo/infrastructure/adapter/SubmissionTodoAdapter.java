package com.ohgiraffer.todo.infrastructure.adapter;

import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.todo.application.port.SubmissionTodoPort;
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
import java.util.Optional;

/* comment.
 *  SubmissionTodoPort 실구현체
 *  - 훈련생 전용 (운영진은 진행률 관점이라 TODO 대상 아님, 기존 설계 그대로 유지)
 *  - targetScope=INDIVIDUAL이면 본인 제출 여부, TEAM이면 소속 팀 제출 여부로 판단
 *  - SubmissionBox 도메인엔 별도 lifecycle status 필드가 없어 dueAt 기준으로 "진행중"/"마감"을 직접 파생시킴
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionTodoAdapter implements SubmissionTodoPort {

    private final SubmissionBoxRepository submissionBoxRepository;  // 제출함 도메인 Repository
    private final SubmissionRepository submissionRepository;        // 제출물 도메인 Repository (제출 여부 확인용)
    private final StudentTeamRepository studentTeamRepository;      // TEAM 단위 제출함일 때 소속 팀 조회용

    // 미제출 발표자료/평가만족도 건수 요약
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        List<TodoItemResponse> pendingItems = getPendingItems(userId, role);  // 상세 리스트 재활용해서 건수 산출
        LocalDateTime nearestDueTime = pendingItems.stream()
                .map(TodoItemResponse::dueOrEventTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        return new TodoResponse(TodoSourceDomain.SUBMISSION, "제출물", pendingItems.size(), nearestDueTime);
    }

    // 미제출 상세 리스트 (훈련생 전용, targetScope별로 제출 여부 판단 방식 분기)
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        if (role != Role.STUDENT) {
            return List.of();  // 운영진은 진행률 관점이라 TODO 대상 아님
        }

        List<SubmissionBox> boxes = submissionBoxRepository.findAll();

        return boxes.stream()
                .filter(box -> !isSubmitted(box, userId))  // 미제출 건만 필터링
                .map(this::toTodoItemResponse)
                .toList();
    }

    // targetScope에 따라 제출 여부 확인 방식 분기
    private boolean isSubmitted(SubmissionBox box, Long userId) {
        if (box.getTargetScope() == SubmissionTargetScope.INDIVIDUAL) {
            return submissionRepository.existsBySubmissionBoxIdAndOwnerUserId(box.getId(), userId);
        }

        // TEAM 단위 - 소속 팀이 없으면 제출 불가 상태이므로 "미제출"로 간주
        Optional<Long> teamId = studentTeamRepository.findActiveTeamIdByStudentId(userId);
        return teamId.isPresent()
                && submissionRepository.existsBySubmissionBoxIdAndTeamId(box.getId(), teamId.get());
    }

    // SubmissionBox 도메인 모델 -> TodoItemResponse 변환
    private TodoItemResponse toTodoItemResponse(SubmissionBox box) {
        String status = LocalDateTime.now().isAfter(box.getDueAt()) ? "마감" : "진행중";  // 별도 status 필드 없어 dueAt 기준 파생

        return new TodoItemResponse(
                TodoSourceDomain.SUBMISSION,
                box.getId(),
                box.getProjectName(),
                status,
                box.getStartAt(),
                box.getDueAt()
        );
    }

}