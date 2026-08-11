package com.ohgiraffer.team.domain.model;

public class UnassignedStudent {

    private final Long userId;
    private final String name;
    private final String email;
    private final String profileImg;

    private UnassignedStudent(
            Long userId,
            String name,
            String email,
            String profileImg
    ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImg = profileImg;
    }

    public static UnassignedStudent restore(
            Long userId,
            String name,
            String email,
            String profileImg
    ) {
        return new UnassignedStudent(
                userId,
                name,
                email,
                profileImg
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

    public String getProfileImg() {
        return profileImg;
    }
}