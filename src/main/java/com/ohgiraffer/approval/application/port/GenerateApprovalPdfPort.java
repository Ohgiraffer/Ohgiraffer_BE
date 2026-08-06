package com.ohgiraffer.approval.application.port;

import com.ohgiraffer.approval.application.query.LeavePdfData;

public interface GenerateApprovalPdfPort {

    byte[] generateLeaveApplicationPdf(
            LeavePdfData data
    );
}