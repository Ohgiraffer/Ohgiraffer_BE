package com.ohgiraffer.team.application.usecase;

public record UnassignedStudentResult(
        Long userId,
        String name,
        String email,
        String profileImgUrl
) {
}