package com.ohgiraffer.user.infrastructure.adapter;

import com.ohgiraffer.consultation.application.port.GetUserInfoPort;
import com.ohgiraffer.consultation.domain.model.UserSummary;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import com.ohgiraffer.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserInfoPortAdapter implements GetUserInfoPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final S3UrlResolver s3UrlResolver;

    @Override
    public String getUserName(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(UserJpaEntity::getName)
                .orElse(null);
    }

    @Override
    public List<UserSummary> getUsersByRole(List<String> roles) {
        List<Role> roleEnums = roles.stream()
                .map(Role::valueOf)
                .toList();

        return springDataUserRepository.findAllByRoleIn(roleEnums).stream()
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getName(),
                        u.getRole(),
                        s3UrlResolver.resolve(u.getProfileImg())
                ))
                .toList();
    }

    @Override
    public Map<Long, String> getNames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return springDataUserRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, UserJpaEntity::getName));
    }

    @Override
    public Optional<UserSummary> getUserSummary(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getName(),
                        u.getRole(),
                        s3UrlResolver.resolve(u.getProfileImg())
                ));
    }
}