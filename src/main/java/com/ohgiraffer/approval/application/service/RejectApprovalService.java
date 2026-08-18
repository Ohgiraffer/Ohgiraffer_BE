package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.RejectApprovalUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RejectApprovalService implements RejectApprovalUseCase {

    private static final int MAX_REJECTION_REASON_LENGTH = 1000;
    private static final String RELATED_ENTITY_TYPE = "APPROVAL";

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public RejectApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            Clock clock,
            ApplicationEventPublisher eventPublisher
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CreateApprovalResult reject(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId,
            String rejectionReason
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                approvalId,
                rejectionReason
        );

        ApprovalRequest approvalRequest =
                approvalRequestRepository.findById(
                                approvalId
                        )
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND
                        ));

        validateRejectAuthority(
                loginUserId,
                loginUserRole,
                approvalRequest
        );

        ApprovalStatus oldStatus =
                approvalRequest.getStatus();

        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        approvalRequest.reject(
                rejectionReason,
                now
        );

        ApprovalRequest savedApprovalRequest =
                approvalRequestRepository.save(
                        approvalRequest
                );

        ApprovalHistory approvalHistory =
                ApprovalHistory.statusChanged(
                        savedApprovalRequest.getId(),
                        loginUserId,
                        oldStatus,
                        savedApprovalRequest.getStatus(),
                        "결재 반려",
                        now
                );

        approvalHistoryRepository.save(
                approvalHistory
        );

        // 반려 완료 - 신청자에게 알림 발행
        eventPublisher.publishEvent(new NotificationRequestedEvent(
                savedApprovalRequest.getRequesterId(),
                NotificationType.APPROVAL_RESULT,
                "결재가 반려되었습니다",
                savedApprovalRequest.getTitle() + " 요청이 반려되었습니다. 사유: " + savedApprovalRequest.getRejectionReason(),
                RELATED_ENTITY_TYPE,
                savedApprovalRequest.getId()
        ));

        return CreateApprovalResult.from(
                savedApprovalRequest
        );
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId,
            String rejectionReason
    ) {
        if (loginUserId == null || loginUserRole == null || approvalId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "반려 사유는 필수입니다."
            );
        }

        if (rejectionReason.strip().length() > MAX_REJECTION_REASON_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "반려 사유는 1000자를 초과할 수 없습니다."
            );
        }
    }

    private void validateRejectAuthority(
            Long loginUserId,
            Role loginUserRole,
            ApprovalRequest approvalRequest
    ) {
        if (!canProcessApproval(
                loginUserRole
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }

        if (approvalRequest.getApproverId() == null
                || !approvalRequest.getApproverId().equals(
                loginUserId
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }
    }

    private boolean canProcessApproval(
            Role role
    ) {
        return role == Role.INSTRUCTOR || role == Role.MANAGER;
    }
}