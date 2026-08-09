package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.port.ApplyApprovedLeavePort;
import com.ohgiraffer.approval.application.usecase.ApproveApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.domain.model.approval.*;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ApproveApprovalService implements ApproveApprovalUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final Clock clock;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final ApplyApprovedLeavePort applyApprovedLeavePort;

    public ApproveApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            Clock clock, ApprovalLeaveDetailRepository approvalLeaveDetailRepository, ApplyApprovedLeavePort applyApprovedLeavePort
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.clock = clock;
        this.approvalLeaveDetailRepository = approvalLeaveDetailRepository;
        this.applyApprovedLeavePort = applyApprovedLeavePort;
    }

    @Override
    @Transactional
    public CreateApprovalResult approve(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                approvalId
        );

        ApprovalRequest approvalRequest =
                approvalRequestRepository.findById(
                                approvalId
                        )
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND
                        ));

        validateApproveAuthority(
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

        approvalRequest.approve(
                now
        );

        ApprovalRequest savedApprovalRequest =
                approvalRequestRepository.save(
                        approvalRequest
                );

        if (savedApprovalRequest.getRequestType() == ApprovalType.LEAVE) {
            ApprovalLeaveDetail leaveDetail = approvalLeaveDetailRepository
                    .findByApprovalId(savedApprovalRequest.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));

            applyApprovedLeavePort.applyApprovedLeave(
                    savedApprovalRequest.getRequesterId(),
                    leaveDetail.getStartDate(),
                    leaveDetail.getEndDate(),
                    savedApprovalRequest.getId()
            );
        }

        ApprovalHistory approvalHistory =
                ApprovalHistory.statusChanged(
                        savedApprovalRequest.getId(),
                        loginUserId,
                        oldStatus,
                        savedApprovalRequest.getStatus(),
                        "결재 승인",
                        now
                );

        approvalHistoryRepository.save(
                approvalHistory
        );

        return CreateApprovalResult.from(
                savedApprovalRequest
        );
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        if (loginUserId == null || loginUserRole == null || approvalId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void validateApproveAuthority(
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