package com.ohgiraffer.team.domain.model;

public class UnassignedStudent {

    private final Long userId;
    private final String name;
    private final String email;

    private UnassignedStudent(
            Long userId,
            String name,
            String email
    ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public static UnassignedStudent restore(
            Long userId,
            String name,
            String email
    ) {
        return new UnassignedStudent(
                userId,
                name,
                email
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}