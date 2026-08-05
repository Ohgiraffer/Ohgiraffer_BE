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

    // 비밀번호 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.needResetPw = false;
    }

    // 알림 수신 여부 변경
    public void setAlarm() {
        this.notificationOn = !this.notificationOn;
    }

    // 프로필 이미지 등록/수정
    public void updateProfileImg(String profileImgKey) {
        this.profileImg = profileImgKey;
    }

    // 프로필 이미지 삭제
    public void deleteProfileImg() {
        this.profileImg = null;
    }
}
