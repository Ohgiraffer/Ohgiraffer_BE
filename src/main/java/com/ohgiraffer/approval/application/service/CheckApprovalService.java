package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.usecase.CheckApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

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

        ApprovalStatus oldStatus =
                approvalRequest.getStatus();

        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        approvalRequest.check(
                loginUserId,
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
                        "결재 확인",
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
        Long loginUserBootcampId = findBootcampId(
                loginUserId
        );

        Long requesterBootcampId = findBootcampId(
                requesterId
        );

        return loginUserBootcampId.equals(
                requesterBootcampId
        );
    }

    private Long findBootcampId(
            Long userId
    ) {
        return userRepository.findBootcampIdByUserId(
                        userId
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND
                ));
    }
}