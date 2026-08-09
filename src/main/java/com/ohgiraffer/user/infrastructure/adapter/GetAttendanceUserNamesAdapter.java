package com.ohgiraffer.user.infrastructure.adapter;

import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import com.ohgiraffer.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAttendanceUserNamesAdapter implements GetUserNamesPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Map<Long, String> findNamesByUserIds(List<Long> userIds) {
        return springDataUserRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, UserJpaEntity::getName));
    }
}