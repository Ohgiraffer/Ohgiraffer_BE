package com.ohgiraffer.approval.application.query;

public record LeavePdfData(
        Long approvalId,
        String studentName,
        String birthDate,
        String phoneNumber,
        String courseName,
        String leaveStartDate,
        String leaveEndDate,
        Integer leaveDays,
        String requestedDate,
        String approverName,
        String requesterSignatureImage,
        String approverSignatureImage
) {
}