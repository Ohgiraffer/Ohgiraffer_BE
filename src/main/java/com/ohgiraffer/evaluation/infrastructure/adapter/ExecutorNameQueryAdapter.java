package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.application.port.ExecutorNameQueryPort;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExecutorNameQueryAdapter implements ExecutorNameQueryPort {

    private final UserRepository userRepository;

    public ExecutorNameQueryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<Long, String> findNames(Collection<Long> userIds) {
        List<Long> distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findByIdIn(distinctIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getName,
                        (first, second) -> first
                ));
    }
}
