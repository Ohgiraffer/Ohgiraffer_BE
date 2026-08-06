package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;

import java.util.List;

public interface ApprovalHistoryRepository {

    ApprovalHistory save(
            ApprovalHistory approvalHistory
    );

    List<ApprovalHistory> findAllByApprovalId(
            Long approvalId
    );
}