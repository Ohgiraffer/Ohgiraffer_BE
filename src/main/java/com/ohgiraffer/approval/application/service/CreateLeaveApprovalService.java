package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.CreateLeaveApprovalUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CreateLeaveApprovalService
        implements CreateLeaveApprovalUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserSignatureRepository userSignatureRepository;
    private final Clock clock;

    public CreateLeaveApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalLeaveDetailRepository approvalLeaveDetailRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            UserSignatureRepository userSignatureRepository,
            Clock clock
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalLeaveDetailRepository = approvalLeaveDetailRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userSignatureRepository = userSignatureRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateApprovalResult create(
            CreateLeaveApprovalCommand command
    ) {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        validateDates(
                command.startDate(),
                command.endDate(),
                now.toLocalDate()
        );

        UserSignature userSignature =
                userSignatureRepository
                        .findActiveByUserId(
                                command.requesterId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SIGNATURE_NOT_FOUND
                                )
                        );

        ApprovalRequest approvalRequest =
                ApprovalRequest.createLeave(
                        command.requesterId(),
                        userSignature.getId(),
                        userSignature.getSignatureImage(),
                        userSignature.getFileType(),
                        now
                );

        ApprovalRequest savedApprovalRequest =
                approvalRequestRepository.save(
                        approvalRequest
                );

        ApprovalLeaveDetail leaveDetail =
                ApprovalLeaveDetail.create(
                        savedApprovalRequest.getId(),
                        command.startDate(),
                        command.endDate()
                );

        approvalLeaveDetailRepository.save(
                leaveDetail
        );

        ApprovalHistory approvalHistory =
                ApprovalHistory.created(
                        savedApprovalRequest.getId(),
                        command.requesterId(),
                        savedApprovalRequest.getStatus(),
                        now
                );

        approvalHistoryRepository.save(
                approvalHistory
        );

        return CreateApprovalResult.from(
                savedApprovalRequest
        );
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today
    ) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "휴가 시작일과 종료일은 필수입니다."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "휴가 시작일은 종료일보다 이후일 수 없습니다."
            );
        }

        if (startDate.isBefore(today)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "지난 날짜로는 휴가를 신청할 수 없습니다."
            );
        }
    }
}