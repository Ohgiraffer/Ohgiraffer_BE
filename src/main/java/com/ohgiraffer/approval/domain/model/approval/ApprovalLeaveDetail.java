package com.ohgiraffer.approval.domain.model.approval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalLeaveDetail {

    private final Long id;
    private final Long approvalId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public static ApprovalLeaveDetail create(
            Long approvalId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new ApprovalLeaveDetail(
                null,
                approvalId,
                startDate,
                endDate
        );
    }

    public static ApprovalLeaveDetail restore(
            Long id,
            Long approvalId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new ApprovalLeaveDetail(
                id,
                approvalId,
                startDate,
                endDate
        );
    }

    public long calculateLeaveDays() {
        return endDate.toEpochDay()
                - startDate.toEpochDay()
                + 1;
    }
}