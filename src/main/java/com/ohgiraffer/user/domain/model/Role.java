package com.ohgiraffer.user.domain.model;

public enum Role {
    INSTRUCTOR,
    MANAGER,
    STUDENT;

    public static Role fromSheetDisplayName(String displayName) {
        if (displayName == null) return null;
        return switch (displayName.trim()) {
            case "훈련생","학생" -> STUDENT;
            case "강사" -> INSTRUCTOR;
            case "매니저","운영진" -> MANAGER;
            default -> null;
        };
    }
}