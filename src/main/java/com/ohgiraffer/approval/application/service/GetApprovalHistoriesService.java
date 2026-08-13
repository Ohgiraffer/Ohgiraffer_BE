package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.GetApprovalHistoriesQuery;
import com.ohgiraffer.approval.application.result.ApprovalHistoryItemResult;
import com.ohgiraffer.approval.application.result.ApprovalHistoryListResult;
import com.ohgiraffer.approval.application.usecase.GetApprovalHistoriesUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetApprovalHistoriesService implements GetApprovalHistoriesUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserRepository userRepository;

    @Override
    public ApprovalHistoryListResult getHistories(
            GetApprovalHistoriesQuery query
    ) {
        validateQuery(
                query
        );

        ApprovalRequest approvalRequest = findApprovalRequest(
                query.approvalId()
        );

        validateReadable(
                approvalRequest,
                query.loginUserId(),
                query.loginUserRole()
        );

        List<ApprovalHistoryItemResult> histories =
                approvalHistoryRepository.findAllByApprovalIdOrderByChangedAtAsc(
                                approvalRequest.getId()
                        )
                        .stream()
                        .map(
                                ApprovalHistoryItemResult::from
                        )
                        .toList();

        return ApprovalHistoryListResult.from(
                histories
        );
    }

    private void validateQuery(
            GetApprovalHistoriesQuery query
    ) {
        if (query == null
                || query.approvalId() == null
                || query.loginUserId() == null
                || query.loginUserRole() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private ApprovalRequest findApprovalRequest(
            Long approvalId
    ) {
        return approvalRequestRepository.findById(
                        approvalId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND
                        )
                );
    }

    private void validateReadable(
            ApprovalRequest approvalRequest,
            Long loginUserId,
            Role loginUserRole
    ) {
        if (approvalRequest.isRequestedBy(
                loginUserId
        )) {
            return;
        }

        if (approvalRequest.isApprovedBy(
                loginUserId
        )) {
            return;
        }

        if (canProcessApproval(
                loginUserRole
        ) && approvalRequest.getStatus() == ApprovalStatus.PENDING) {
            validateSameBootcamp(
                    approvalRequest.getRequesterId(),
                    loginUserId
            );
            return;
        }

        throw new BusinessException(
                ErrorCode.APPROVAL_ACCESS_DENIED
        );
    }

    private boolean canProcessApproval(
            Role role
    ) {
        return role == Role.INSTRUCTOR
                || role == Role.MANAGER;
    }

    private void validateSameBootcamp(
            Long requesterId,
            Long loginUserId
    ) {
        if (!isSameBootcamp(
                requesterId,
                loginUserId
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }
    }

    private boolean isSameBootcamp(
            Long requesterId,
            Long loginUserId
    ) {
        Optional<Long> requesterBootcampId =
                userRepository.findBootcampIdByUserId(
                        requesterId
                );

        Optional<Long> loginUserBootcampId =
                userRepository.findBootcampIdByUserId(
                        loginUserId
                );

        if (requesterBootcampId.isEmpty()
                || loginUserBootcampId.isEmpty()) {
            return false;
        }

        return requesterBootcampId.get().equals(
                loginUserBootcampId.get()
        );
    }
}