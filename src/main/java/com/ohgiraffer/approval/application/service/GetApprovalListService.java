package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.ApprovalListItemResult;
import com.ohgiraffer.approval.application.query.ApprovalListScope;
import com.ohgiraffer.approval.application.usecase.GetApprovalListUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalPurchaseDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetApprovalListService implements GetApprovalListUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<ApprovalListItemResult> getApprovals(
            Long loginUserId,
            Role loginUserRole,
            ApprovalListScope scope
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                scope
        );

        List<ApprovalRequest> approvalRequests = findApprovalRequests(
                loginUserId,
                scope
        );

        Map<Long, BudgetCategory> budgetCategoryById = budgetCategoryRepository.findAll()
                .stream()
                .collect(
                        Collectors.toMap(
                                BudgetCategory::getId,
                                category -> category
                        )
                );

        return approvalRequests.stream()
                .map(
                        approvalRequest -> toListItemResult(
                                approvalRequest,
                                budgetCategoryById
                        )
                )
                .toList();
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            ApprovalListScope scope
    ) {
        if (loginUserId == null || loginUserRole == null || scope == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (scope == ApprovalListScope.PROCESSING
                && !canProcessApproval(
                loginUserRole
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

    private List<ApprovalRequest> findApprovalRequests(
            Long loginUserId,
            ApprovalListScope scope
    ) {
        if (scope == ApprovalListScope.REQUESTED) {
            return approvalRequestRepository.findByRequesterIdOrderByRequestedAtDesc(
                    loginUserId
            );
        }

        if (scope == ApprovalListScope.PROCESSING) {
            return approvalRequestRepository.findProcessingApprovals(
                    loginUserId
            );
        }

        throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE
        );
    }

    private ApprovalListItemResult toListItemResult(
            ApprovalRequest approvalRequest,
            Map<Long, BudgetCategory> budgetCategoryById
    ) {
        String budgetCategoryName = null;
        BigDecimal amount = null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (approvalRequest.getRequestType() == ApprovalType.LEAVE) {
            ApprovalLeaveDetail leaveDetail = approvalLeaveDetailRepository.findByApprovalId(
                            approvalRequest.getId()
                    )
                    .orElse(null);

            if (leaveDetail != null) {
                startDate = leaveDetail.getStartDate();
                endDate = leaveDetail.getEndDate();
            }
        }

        if (approvalRequest.getRequestType() == ApprovalType.PURCHASE) {
            ApprovalPurchaseDetail purchaseDetail = approvalPurchaseDetailRepository.findByApprovalId(
                            approvalRequest.getId()
                    )
                    .orElse(null);

            if (purchaseDetail != null) {
                amount = purchaseDetail.getAmount();

                BudgetCategory budgetCategory = budgetCategoryById.get(
                        purchaseDetail.getBudgetCategoryId()
                );

                if (budgetCategory != null) {
                    budgetCategoryName = budgetCategory.getName();
                }
            }
        }

        return new ApprovalListItemResult(
                approvalRequest.getId(),
                approvalRequest.getRequestType(),
                approvalRequest.getStatus(),
                approvalRequest.getTitle(),
                approvalRequest.getRequesterId(),
                findUserName(
                        approvalRequest.getRequesterId()
                ),
                approvalRequest.getApproverId(),
                findUserNameOrNull(
                        approvalRequest.getApproverId()
                ),
                budgetCategoryName,
                amount,
                startDate,
                endDate,
                approvalRequest.getRequestedAt()
        );
    }

    private String findUserName(
            Long userId
    ) {
        return userRepository
                .findById(
                        userId
                )
                .map(
                        User::getName
                )
                .orElse(
                        ""
                );
    }

    private String findUserNameOrNull(
            Long userId
    ) {
        if (userId == null) {
            return null;
        }

        return findUserName(
                userId
        );
    }
}