package com.ohgiraffer.user.infrastructure.persistence;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "need_reset_pw", nullable = false)
    private boolean needResetPw;

    @Column(name = "notification_on", nullable = false)
    private boolean notificationOn;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "bootcamp_id")
    private Long bootcampId;

    private UserJpaEntity(
            Long id, String name, String phone, String email, Role role,
            String profileImg, String password, boolean needResetPw,
            boolean notificationOn, LocalDate joinDate, LocalDate leaveDate, UserStatus status, Long bootcampId
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
        this.bootcampId = bootcampId;
    }

    // 도메인 User -> UserEntity
    public static UserJpaEntity fromDomain(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImg(),
                user.getPassword(),
                user.isNeedResetPw(),
                user.isNotificationOn(),
                user.getJoinDate(),
                user.getLeaveDate(),
                user.getStatus(),
                user.getBootcampId()
        );
    }

    // UserEntity -> 도메인 User
    public User toDomain() {
        return new User(
                id, name, phone, email, role, profileImg, password,
                needResetPw, notificationOn, joinDate, leaveDate, status, bootcampId
        );
    }
}
