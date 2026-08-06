package com.ohgiraffer.user.infrastructure.adapter;

import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetUserBootcampIdAdapter implements GetUserBootcampIdPort {
    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<Long> findBootcampIdByUserId(Long userId) {
        return springDataUserRepository.findBootcampIdByUserId(userId);
    }
}
