package com.ohgiraffer.user.infrastructure.adapter;

import com.ohgiraffer.consultation.application.port.GetUserInfoPort;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import com.ohgiraffer.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserInfoPortAdapter implements GetUserInfoPort {

    private final SpringDataUserRepository springDataUserRepository;

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
                .map(u -> new UserSummary(u.getId(), u.getName(), u.getRole().name()))
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
}