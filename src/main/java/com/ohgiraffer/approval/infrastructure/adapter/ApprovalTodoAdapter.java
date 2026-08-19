package com.ohgiraffer.approval.infrastructure.adapter;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.todo.application.port.ApprovalTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/*
 * comment.
 *  ApprovalTodoPort 실구현체
 *  - 훈련생: findByRequesterIdOrderByRequestedAtDesc로 본인 신청 전체 조회 후 COMPLETED/REJECTED 아닌 건만 "처리중"으로 필터링
 *  - 강사/매니저: findProcessingApprovals(userId, bootcampId)로 처리 대상 목록 그대로 사용
 *  - bootcampId는 결재 조회에 필수 파라미터라 GetUserBootcampIdPort로 먼저 조회해서 넘김
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalTodoAdapter implements ApprovalTodoPort {

    // "처리중"으로 인정할 상태 화이트리스트. PENDING(신청됨), CHECKED(확인됨)만 포함
    private static final Set<ApprovalStatus> STILL_PROCESSING_STATUSES =
            Set.of(ApprovalStatus.PENDING, ApprovalStatus.CHECKED);

    private final ApprovalRequestRepository approvalRequestRepository;  // 결재 도메인 Repository 직접 주입
    private final GetUserBootcampIdPort getUserBootcampIdPort;          // userId -> bootcampId 조회용 Port

    // role별 결재 건수 요약
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        List<TodoItemResponse> pendingItems = getPendingItems(userId, role);  // role별 처리중 목록 재사용
        String label = role == Role.MANAGER ? "결재 대기" : "결재 처리중";     // 매니저는 승인권자 관점, 나머지는 신청자 관점 라벨
        return new TodoResponse(TodoSourceDomain.APPROVAL, label, pendingItems.size(), null);  // 결재는 페이지네이션 없음
    }

    // role별 결재 상세 리스트
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        List<ApprovalRequest> requests;

        if (role == Role.MANAGER) {
            // 매니저만 실제 승인권자 - "처리해야 할" 결재 대기 목록
            Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                    .orElse(null);
            if (bootcampId == null) {
                return List.of();  // 소속 부트캠프가 없으면 조회 불가로 빈 목록 반환
            }
            // be1 쿼리가 status 조건 없이 approverId 매칭 건을 모두 내려줄 수 있어
            // TODO 도메인 단에서 한 번 더 화이트리스트 필터링
            requests = approvalRequestRepository.findProcessingApprovals(userId, bootcampId).stream()
                    .filter(this::isStillProcessing)
                    .toList();
        } else {
            // STUDENT/INSTRUCTOR 공통 - 본인 신청 건 중 PENDING/CHECKED만 "처리중"
            requests = approvalRequestRepository.findByRequesterIdOrderByRequestedAtDesc(userId).stream()
                    .filter(this::isStillProcessing)
                    .toList();
        }

        return requests.stream()
                .map(this::toTodoItemResponse)
                .toList();
    }

    // 화이트리스트(PENDING, CHECKED)에 포함된 상태만 "처리중"으로 인정
    private boolean isStillProcessing(ApprovalRequest request) {
        return STILL_PROCESSING_STATUSES.contains(request.getStatus());
    }

    // ApprovalRequest 도메인 모델 -> TodoItemResponse 변환
    private TodoItemResponse toTodoItemResponse(ApprovalRequest request) {
        return new TodoItemResponse(
                TodoSourceDomain.APPROVAL,
                request.getId(),
                request.getRequestType().name(),   // LEAVE / PURCHASE
                request.getStatus().name(),         // PENDING/CHECKED/APPROVED/REJECTED/COMPLETED
                request.getRequestedAt(),
                null                                 // 결재는 마감 개념 없음
        );
    }
}
