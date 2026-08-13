package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetUnassignedStudentUseCase {

    List<UnassignedStudentResult> getUnassignedStudents(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId
    );
}