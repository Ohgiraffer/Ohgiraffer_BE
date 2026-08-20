package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.usecase.CheckApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.aop.auditlog.Audited;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CheckApprovalService implements CheckApprovalUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public CheckApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    @Audited(
            domain = "approval",
            eventType = "APPROVAL_CHECK",
            targetId = "#approvalId",
            afterValue = "#result.status"
    )
    public CreateApprovalResult check(
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

        validateCheckAuthority(
                loginUserId,
                loginUserRole,
                approvalRequest
        );

        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        int updatedCount =
                approvalRequestRepository.checkPendingApproval(
                        approvalRequest.getId(),
                        loginUserId,
                        now
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_INVALID_STATUS
            );
        }

        ApprovalRequest checkedApprovalRequest =
                approvalRequestRepository.findById(
                                approvalId
                        )
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND
                        ));

        ApprovalHistory approvalHistory =
                ApprovalHistory.statusChanged(
                        checkedApprovalRequest.getId(),
                        loginUserId,
                        ApprovalStatus.PENDING,
                        checkedApprovalRequest.getStatus(),
                        "결재 확인",
                        now
                );

        approvalHistoryRepository.save(
                approvalHistory
        );

        return CreateApprovalResult.from(
                checkedApprovalRequest
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

    private void validateCheckAuthority(
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

        if (!isSameBootcamp(
                loginUserId,
                approvalRequest.getRequesterId()
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

    private boolean isSameBootcamp(
            Long loginUserId,
            Long requesterId
    ) {
        Optional<Long> loginUserBootcampId =
                userRepository.findBootcampIdByUserId(
                        loginUserId
                );

        Optional<Long> requesterBootcampId =
                userRepository.findBootcampIdByUserId(
                        requesterId
                );

        if (loginUserBootcampId.isEmpty()
                || requesterBootcampId.isEmpty()) {
            return false;
        }

        return loginUserBootcampId.get().equals(
                requesterBootcampId.get()
        );
    }
}