package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryItemResult;
import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryResult;
import com.ohgiraffer.approval.application.usecase.GetTraineeApprovalHistoryUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTraineeApprovalHistoryService implements GetTraineeApprovalHistoryUseCase {

    private static final String LEAVE_APPROVAL_TYPE_NAME = "휴가 결재";

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final UserRepository userRepository;

    @Override
    public TraineeApprovalHistoryResult getApprovalHistory(
            Long loginUserId,
            Role loginUserRole,
            Long traineeId
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                traineeId
        );

        User loginUser = findUser(
                loginUserId
        );

        User trainee = findUser(
                traineeId
        );

        validateAccess(
                loginUser,
                loginUserRole,
                trainee
        );

        List<ApprovalRequest> approvals =
                approvalRequestRepository.findByRequesterIdAndRequestTypeAndStatusInOrderByRequestedAtDesc(
                        traineeId,
                        ApprovalType.LEAVE,
                        List.of(
                                ApprovalStatus.APPROVED,
                                ApprovalStatus.COMPLETED
                        )
                );

        if (approvals.isEmpty()) {
            return new TraineeApprovalHistoryResult(
                    List.of()
            );
        }

        List<Long> approvalIds = approvals.stream()
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

        List<TraineeApprovalHistoryItemResult> items =
                approvals.stream()
                        .map(
                                approval -> toResult(
                                        approval,
                                        leaveDetailByApprovalId.get(
                                                approval.getId()
                                        )
                                )
                        )
                        .toList();

        return new TraineeApprovalHistoryResult(
                items
        );
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            Long traineeId
    ) {
        if (loginUserId == null
                || loginUserRole == null
                || traineeId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (!canViewTraineeApprovalHistory(
                loginUserRole
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }
    }

    private boolean canViewTraineeApprovalHistory(
            Role role
    ) {
        return role == Role.INSTRUCTOR
                || role == Role.MANAGER;
    }

    private User findUser(
            Long userId
    ) {
        return userRepository.findById(
                        userId
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND
                ));
    }

    private void validateAccess(
            User loginUser,
            Role loginUserRole,
            User trainee
    ) {
        if (trainee.getRole() != Role.STUDENT) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        if (loginUser.getBootcampId() == null
                || trainee.getBootcampId() == null
                || !loginUser.getBootcampId().equals(
                trainee.getBootcampId()
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }

        if (!canViewTraineeApprovalHistory(
                loginUserRole
        )) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_ACCESS_DENIED
            );
        }
    }

    private TraineeApprovalHistoryItemResult toResult(
            ApprovalRequest approval,
            ApprovalLeaveDetail leaveDetail
    ) {
        if (leaveDetail == null) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_NOT_FOUND
            );
        }

        return new TraineeApprovalHistoryItemResult(
                approval.getId(),
                toLocalDate(
                        approval.getRequestedAt()
                ),
                LEAVE_APPROVAL_TYPE_NAME,
                leaveDetail.getStartDate(),
                leaveDetail.getEndDate(),
                leaveDetail.calculateLeaveDays(),
                toLocalDate(
                        approval.getProcessedAt()
                ),
                approval.getStatus()
        );
    }

    private LocalDate toLocalDate(
            java.time.LocalDateTime dateTime
    ) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.toLocalDate();
    }
}