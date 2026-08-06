package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.LeavePdfData;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalPdfDataReaderService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy.MM.dd"
            );

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final UserRepository userRepository;
    private final UserSignatureRepository userSignatureRepository;
    private final BootcampRepository bootcampRepository;

    public LeavePdfData getLeavePdfData(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                approvalId
        );

        ApprovalRequest approvalRequest = findApprovalRequest(
                approvalId
        );

        validateAccess(
                loginUserId,
                loginUserRole,
                approvalRequest
        );

        validateLeaveApproval(
                approvalRequest
        );

        ApprovalLeaveDetail leaveDetail = findLeaveDetail(
                approvalRequest.getId()
        );

        User requester = findUser(
                approvalRequest.getRequesterId()
        );

        return new LeavePdfData(
                approvalRequest.getId(),
                requester.getName(),
                findCourseName(
                        requester
                ),
                leaveDetail.getStartDate().format(
                        DATE_FORMATTER
                ),
                leaveDetail.getEndDate().format(
                        DATE_FORMATTER
                ),
                Math.toIntExact(
                        leaveDetail.calculateLeaveDays()
                ),
                approvalRequest.getRequestedAt().toLocalDate().format(
                        DATE_FORMATTER
                ),
                findApproverName(
                        approvalRequest.getApproverId()
                ),
                toRequesterSignatureDataUri(
                        approvalRequest
                ),
                toApproverSignatureDataUri(
                        approvalRequest.getApproverId()
                )
        );
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        if (loginUserId == null
                || loginUserRole == null
                || approvalId == null) {
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

    private void validateAccess(
            Long loginUserId,
            Role loginUserRole,
            ApprovalRequest approvalRequest
    ) {
        if (approvalRequest.getRequesterId().equals(
                loginUserId
        )) {
            return;
        }

        if (approvalRequest.getApproverId() != null
                && approvalRequest.getApproverId().equals(
                loginUserId
        )) {
            return;
        }

        if (approvalRequest.getStatus() == ApprovalStatus.PENDING
                && canProcessApproval(
                loginUserRole
        )
                && isSameBootcamp(
                loginUserId,
                approvalRequest.getRequesterId()
        )) {
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

    private void validateLeaveApproval(
            ApprovalRequest approvalRequest
    ) {
        if (approvalRequest.getRequestType() != ApprovalType.LEAVE) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private ApprovalLeaveDetail findLeaveDetail(
            Long approvalId
    ) {
        return approvalLeaveDetailRepository.findByApprovalId(
                        approvalId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND
                        )
                );
    }

    private User findUser(
            Long userId
    ) {
        return userRepository.findById(
                        userId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private String findApproverName(
            Long approverId
    ) {
        if (approverId == null) {
            return "";
        }

        return userRepository.findById(
                        approverId
                )
                .map(
                        User::getName
                )
                .orElse(
                        ""
                );
    }

    private String findCourseName(
            User requester
    ) {
        if (requester.getBootcampId() == null) {
            return "";
        }

        return bootcampRepository.findById(
                        requester.getBootcampId()
                )
                .map(
                        Bootcamp::getProName
                )
                .orElse(
                        ""
                );
    }

    private String toRequesterSignatureDataUri(
            ApprovalRequest approvalRequest
    ) {
        byte[] signatureImage = approvalRequest.getSignatureImageSnapshot();

        if (signatureImage == null
                || approvalRequest.getSignatureFileTypeSnapshot() == null) {
            return null;
        }

        return toDataUri(
                signatureImage,
                approvalRequest.getSignatureFileTypeSnapshot()
        );
    }

    private String toApproverSignatureDataUri(
            Long approverId
    ) {
        if (approverId == null) {
            return null;
        }

        return userSignatureRepository.findActiveByUserId(
                        approverId
                )
                .map(
                        signature -> toDataUri(
                                signature.getSignatureImage(),
                                signature.getFileType()
                        )
                )
                .orElse(
                        null
                );
    }

    private String toDataUri(
            byte[] image,
            String fileType
    ) {
        if (image == null
                || fileType == null) {
            return null;
        }

        return "data:"
                + fileType
                + ";base64,"
                + Base64.getEncoder().encodeToString(
                image
        );
    }
}