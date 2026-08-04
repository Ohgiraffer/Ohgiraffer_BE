package com.ohgiraffer.user.domain.model;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class User {
    private final Long id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private String profileImg;
    private String password;
    private boolean needResetPw;
    private boolean notificationOn;
    private final LocalDate joinDate;
    private LocalDate leaveDate;
    private UserStatus status;

    public User(
            Long id,
            String name,
            String phone,
            String email,
            Role role,
            String profileImg,
            String password,
            boolean needResetPw,
            boolean notificationOn,
            LocalDate joinDate,
            LocalDate leaveDate,
            UserStatus status
    ) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.profileImg = profileImg;
        this.password = password;
        this.needResetPw = needResetPw;
        this.notificationOn = notificationOn;
        this.joinDate = joinDate;
        this.leaveDate = leaveDate;
        this.status = status;
    }

    // 신규 회원 추가
    public static User create(
            String name,
            String phone,
            String email,
            Role role,
            String password
    ) {
        return new User(
                null,
                name,
                phone,
                email,
                role,
                null,
                password,
                true,
                true,
                LocalDate.now(),
                null,
                UserStatus.ACTIVE
        );
    }

}
