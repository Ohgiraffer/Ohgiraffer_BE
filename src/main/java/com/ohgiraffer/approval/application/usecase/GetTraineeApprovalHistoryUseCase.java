package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryResult;
import com.ohgiraffer.user.domain.model.Role;

public interface GetTraineeApprovalHistoryUseCase {

    TraineeApprovalHistoryResult getApprovalHistory(
            Long loginUserId,
            Role loginUserRole,
            Long traineeId
    );
}