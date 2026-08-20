package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.LeavePdfData;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
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
    private final ApprovalApplicantProfileRepository approvalApplicantProfileRepository;
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
                approvalRequest
        );

        validateLeaveApproval(
                approvalRequest
        );

        validatePdfDownloadReady(
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
                findBirthDate(
                        requester.getId()
                ),
                valueOrEmpty(
                        requester.getPhone()
                ),
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

        throw new BusinessException(
                ErrorCode.APPROVAL_ACCESS_DENIED
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

    private void validatePdfDownloadReady(
            ApprovalRequest approvalRequest
    ) {
        if (approvalRequest.getApproverId() == null
                || approvalRequest.getConfirmedAt() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "담당자 확인 후 PDF 다운로드가 가능합니다."
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

    private String findBirthDate(
            Long requesterId
    ) {
        return approvalApplicantProfileRepository.findByUserId(
                        requesterId
                )
                .map(
                        ApprovalApplicantProfile::getBirthDate
                )
                .map(
                        birthDate -> birthDate.format(
                                DATE_FORMATTER
                        )
                )
                .orElse(
                        ""
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
        byte[] signatureImage =
                approvalRequest.getSignatureImageSnapshot();

        if (signatureImage == null
                || approvalRequest.getSignatureFileTypeSnapshot() == null) {
            throw new BusinessException(
                    ErrorCode.SIGNATURE_NOT_FOUND,
                    "신청자 전자서명 정보가 없습니다."
            );
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
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "담당자 확인 후 PDF 다운로드가 가능합니다."
            );
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
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.SIGNATURE_NOT_FOUND,
                                "확인자 전자서명이 등록되어 있지 않습니다."
                        )
                );
    }

    private String toDataUri(
            byte[] image,
            String fileType
    ) {
        if (image == null
                || fileType == null) {
            throw new BusinessException(
                    ErrorCode.SIGNATURE_NOT_FOUND
            );
        }

        return "data:"
                + fileType
                + ";base64,"
                + Base64.getEncoder().encodeToString(
                image
        );
    }

    private String valueOrEmpty(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value;
    }
}