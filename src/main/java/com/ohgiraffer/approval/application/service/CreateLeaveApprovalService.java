package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.port.GetBootcampManagerIdsPort;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.CreateLeaveApprovalUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreateLeaveApprovalService
        implements CreateLeaveApprovalUseCase {

    private static final String RELATED_ENTITY_TYPE = "APPROVAL";

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserSignatureRepository userSignatureRepository;
    private final ApprovalApplicantProfileRepository approvalApplicantProfileRepository;
    private final Clock clock;
    private final UserQueryUsecase userQueryUsecase;
    private final GetBootcampManagerIdsPort getBootcampManagerIdsPort;
    private final ApplicationEventPublisher eventPublisher;

    public CreateLeaveApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalLeaveDetailRepository approvalLeaveDetailRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            UserSignatureRepository userSignatureRepository,
            ApprovalApplicantProfileRepository approvalApplicantProfileRepository,
            Clock clock,
            UserQueryUsecase userQueryUsecase,
            GetBootcampManagerIdsPort getBootcampManagerIdsPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalLeaveDetailRepository = approvalLeaveDetailRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userSignatureRepository = userSignatureRepository;
        this.approvalApplicantProfileRepository = approvalApplicantProfileRepository;
        this.clock = clock;
        this.userQueryUsecase = userQueryUsecase;
        this.getBootcampManagerIdsPort = getBootcampManagerIdsPort;
        this.eventPublisher = eventPublisher;
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

        validateApplicantProfileExists(
                command.requesterId()
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

        notifyManagers(
                savedApprovalRequest,
                command.requesterId()
        );

        return CreateApprovalResult.from(
                savedApprovalRequest
        );
    }

    // 신청자와 같은 부트캠프의 매니저 전원에게 브로드캐스트
    private void notifyManagers(ApprovalRequest savedApprovalRequest, Long requesterId) {
        Long bootcampId = userQueryUsecase.getBootcampId(requesterId);
        List<Long> managerIds = getBootcampManagerIdsPort.findManagerIdsByBootcampId(bootcampId);

        for (Long managerId : managerIds) {
            eventPublisher.publishEvent(new NotificationRequestedEvent(
                    managerId,
                    NotificationType.APPROVAL_REQUEST,
                    "휴가 신청이 접수되었습니다",
                    "휴가 신청이 새로 접수되었습니다. 확인이 필요합니다.",
                    RELATED_ENTITY_TYPE,
                    savedApprovalRequest.getId()
            ));
        }
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

        if (startDate.isAfter(
                endDate
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "휴가 시작일은 종료일보다 이후일 수 없습니다."
            );
        }

        if (startDate.isBefore(
                today
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "지난 날짜로는 휴가를 신청할 수 없습니다."
            );
        }
    }

    private void validateApplicantProfileExists(
            Long requesterId
    ) {
        boolean exists =
                approvalApplicantProfileRepository.findByUserId(
                                requesterId
                        )
                        .isPresent();

        if (!exists) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "휴가 신청을 위해 생년월일을 입력해주세요."
            );
        }
    }
}