package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.port.GenerateApprovalPdfPort;
import com.ohgiraffer.approval.application.query.LeavePdfData;
import com.ohgiraffer.approval.application.result.ApprovalPdfResult;
import com.ohgiraffer.approval.application.usecase.DownloadApprovalPdfUseCase;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadApprovalPdfService implements DownloadApprovalPdfUseCase {

    private final ApprovalPdfDataReaderService approvalPdfDataReaderService;
    private final GenerateApprovalPdfPort generateApprovalPdfPort;

    @Override
    public ApprovalPdfResult downloadPdf(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        LeavePdfData pdfData =
                approvalPdfDataReaderService.getLeavePdfData(
                        loginUserId,
                        loginUserRole,
                        approvalId
                );

        byte[] pdfContent =
                generateApprovalPdfPort.generateLeaveApplicationPdf(
                        pdfData
                );

        return new ApprovalPdfResult(
                createFileName(
                        approvalId
                ),
                pdfContent
        );
    }

    private String createFileName(
            Long approvalId
    ) {
        return "leave-approval-"
                + approvalId
                + ".pdf";
    }
}