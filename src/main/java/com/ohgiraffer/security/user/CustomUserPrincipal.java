package com.ohgiraffer.security.user;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {

    private final Long id;       // PK
    private final String email;      // 로그인 아이디로 쓰는 값
    private final String password;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    private CustomUserPrincipal(
            Long id,
            String email,
            String password,
            Role role,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                user.getStatus() == UserStatus.ACTIVE
                        || user.getStatus() == UserStatus.COMPLETED
        );
    }

    public Long getId() {return id;}

    public Role getRole() {return role;}

    @Override
    public String getUsername() {return email; // 로그인 할 때 쓰는 값 = 이메일
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
