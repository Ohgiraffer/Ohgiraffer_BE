package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;

import java.util.List;
import java.util.Optional;

public interface ApprovalLeaveDetailRepository {

    ApprovalLeaveDetail save(
            ApprovalLeaveDetail approvalLeaveDetail
    );

    Optional<ApprovalLeaveDetail> findByApprovalId(
            Long approvalId
    );

    List<ApprovalLeaveDetail> findByApprovalIdIn(
            List<Long> approvalIds
    );
}