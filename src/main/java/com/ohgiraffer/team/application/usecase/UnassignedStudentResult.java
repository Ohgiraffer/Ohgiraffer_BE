package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.UnassignedStudent;

public record UnassignedStudentResult(
        Long userId,
        String name,
        String email
) {

    public static UnassignedStudentResult from(
            UnassignedStudent student
    ) {
        return new UnassignedStudentResult(
                student.getUserId(),
                student.getName(),
                student.getEmail()
        );
    }
}