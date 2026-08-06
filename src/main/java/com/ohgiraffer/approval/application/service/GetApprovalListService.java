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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

        Long loginUserBootcampId = findLoginUserBootcampId(
                loginUserId
        );

        List<ApprovalRequest> approvalRequests = findApprovalRequests(
                loginUserId,
                loginUserBootcampId,
                scope
        );

        if (approvalRequests.isEmpty()) {
            return List.of();
        }

        List<Long> approvalIds = approvalRequests.stream()
                .map(
                        ApprovalRequest::getId
                )
                .toList();

        Map<Long, ApprovalLeaveDetail> leaveDetailByApprovalId =
                approvalLeaveDetailRepository.findByApprovalIdIn(
                                approvalIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ApprovalLeaveDetail::getApprovalId,
                                        Function.identity()
                                )
                        );

        Map<Long, ApprovalPurchaseDetail> purchaseDetailByApprovalId =
                approvalPurchaseDetailRepository.findByApprovalIdIn(
                                approvalIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ApprovalPurchaseDetail::getApprovalId,
                                        Function.identity()
                                )
                        );

        Map<Long, BudgetCategory> budgetCategoryById = findBudgetCategories(
                purchaseDetailByApprovalId
        );

        Map<Long, String> userNameById = findUserNames(
                approvalRequests
        );

        return approvalRequests.stream()
                .map(
                        approvalRequest -> toListItemResult(
                                approvalRequest,
                                leaveDetailByApprovalId.get(
                                        approvalRequest.getId()
                                ),
                                purchaseDetailByApprovalId.get(
                                        approvalRequest.getId()
                                ),
                                budgetCategoryById,
                                userNameById
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

    private Long findLoginUserBootcampId(
            Long loginUserId
    ) {
        return userRepository.findBootcampIdByUserId(
                        loginUserId
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND
                ));
    }

    private List<ApprovalRequest> findApprovalRequests(
            Long loginUserId,
            Long loginUserBootcampId,
            ApprovalListScope scope
    ) {
        if (scope == ApprovalListScope.REQUESTED) {
            return approvalRequestRepository.findByRequesterIdOrderByRequestedAtDesc(
                    loginUserId
            );
        }

        if (scope == ApprovalListScope.PROCESSING) {
            return approvalRequestRepository.findProcessingApprovals(
                    loginUserId,
                    loginUserBootcampId
            );
        }

        throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE
        );
    }

    private Map<Long, BudgetCategory> findBudgetCategories(
            Map<Long, ApprovalPurchaseDetail> purchaseDetailByApprovalId
    ) {
        List<Long> budgetCategoryIds = purchaseDetailByApprovalId.values()
                .stream()
                .map(
                        ApprovalPurchaseDetail::getBudgetCategoryId
                )
                .distinct()
                .toList();

        if (budgetCategoryIds.isEmpty()) {
            return Map.of();
        }

        return budgetCategoryRepository.findByIdIn(
                        budgetCategoryIds
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                BudgetCategory::getId,
                                Function.identity()
                        )
                );
    }

    private Map<Long, String> findUserNames(
            List<ApprovalRequest> approvalRequests
    ) {
        Set<Long> userIds = new LinkedHashSet<>();

        for (ApprovalRequest approvalRequest : approvalRequests) {
            userIds.add(
                    approvalRequest.getRequesterId()
            );

            if (approvalRequest.getApproverId() != null) {
                userIds.add(
                        approvalRequest.getApproverId()
                );
            }
        }

        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findByIdIn(
                        userIds.stream()
                                .toList()
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                User::getId,
                                User::getName
                        )
                );
    }

    private ApprovalListItemResult toListItemResult(
            ApprovalRequest approvalRequest,
            ApprovalLeaveDetail leaveDetail,
            ApprovalPurchaseDetail purchaseDetail,
            Map<Long, BudgetCategory> budgetCategoryById,
            Map<Long, String> userNameById
    ) {
        String budgetCategoryName = null;

        if (purchaseDetail != null) {
            BudgetCategory budgetCategory = budgetCategoryById.get(
                    purchaseDetail.getBudgetCategoryId()
            );

            if (budgetCategory != null) {
                budgetCategoryName = budgetCategory.getName();
            }
        }

        return new ApprovalListItemResult(
                approvalRequest.getId(),
                approvalRequest.getRequestType(),
                approvalRequest.getStatus(),
                approvalRequest.getTitle(),
                approvalRequest.getRequesterId(),
                userNameById.getOrDefault(
                        approvalRequest.getRequesterId(),
                        ""
                ),
                approvalRequest.getApproverId(),
                approvalRequest.getApproverId() == null
                        ? null
                        : userNameById.getOrDefault(
                        approvalRequest.getApproverId(),
                        ""
                ),
                budgetCategoryName,
                purchaseDetail == null ? null : purchaseDetail.getAmount(),
                leaveDetail == null ? null : leaveDetail.getStartDate(),
                leaveDetail == null ? null : leaveDetail.getEndDate(),
                approvalRequest.getRequestedAt()
        );
    }
}