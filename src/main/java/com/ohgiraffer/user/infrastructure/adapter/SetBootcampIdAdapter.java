package com.ohgiraffer.user.infrastructure.adapter;

import com.ohgiraffer.bootcamp.application.port.SetBootcampIdPort;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import com.ohgiraffer.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SetBootcampIdAdapter implements SetBootcampIdPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    @Transactional
    public boolean assignBootcampIfAbsent(Long userId, Long bootcampId) {
        int updatedRows = springDataUserRepository.assignBootcampIfAbsent(userId, bootcampId);
        return updatedRows > 0;
    }
}