package com.ohgiraffer.approval.infrastructure.adapter;

import com.ohgiraffer.chatbot.application.port.ApprovalActionPort;
import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.usecase.*;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/*
 * comment.
 *  ApprovalActionPort 실구현체
 *  - 전자결재 담당 도메인의 기존 UseCase 4종을 그대로 주입받아 위임만 함
 *  - 이 어댑터는 전자결재 도메인 코드를 전혀 건드리지 않고, 챗봇 쪽에서만 신규로 추가되는 파일임
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalActionAdapter implements ApprovalActionPort {

    private final ApproveApprovalUseCase approveApprovalUseCase;
    private final RejectApprovalUseCase rejectApprovalUseCase;
    private final CheckApprovalUseCase checkApprovalUseCase;
    private final CreateLeaveApprovalUseCase createLeaveApprovalUseCase;

    @Override
    public CreateApprovalResult approve(Long userId, Role role, Long approvalId) {
        log.info("[ChatbotApprovalAction] 승인 처리 요청 | userId={}, approvalId={}", userId, approvalId);
        return approveApprovalUseCase.approve(userId, role, approvalId);
    }

    @Override
    public CreateApprovalResult reject(Long userId, Role role, Long approvalId, String reason) {
        log.info("[ChatbotApprovalAction] 반려 처리 요청 | userId={}, approvalId={}", userId, approvalId);
        return rejectApprovalUseCase.reject(userId, role, approvalId, reason);
    }

    @Override
    public CreateApprovalResult check(Long userId, Role role, Long approvalId) {
        log.info("[ChatbotApprovalAction] 확인 처리 요청 | userId={}, approvalId={}", userId, approvalId);
        return checkApprovalUseCase.check(userId, role, approvalId);
    }

    @Override
    public CreateApprovalResult createLeaveApproval(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("[ChatbotApprovalAction] 휴가 신청 요청 | userId={}, {}~{}", userId, startDate, endDate);
        CreateLeaveApprovalCommand command = new CreateLeaveApprovalCommand(userId, startDate, endDate);
        return createLeaveApprovalUseCase.create(command);
    }

}
