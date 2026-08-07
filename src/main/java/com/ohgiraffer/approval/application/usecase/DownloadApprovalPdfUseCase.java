package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.result.ApprovalPdfResult;
import com.ohgiraffer.user.domain.model.Role;

public interface DownloadApprovalPdfUseCase {

    ApprovalPdfResult downloadPdf(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    );
}