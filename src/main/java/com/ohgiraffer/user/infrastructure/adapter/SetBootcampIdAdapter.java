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
    public void assignBootcamp(Long userId, Long bootcampId) {
        UserJpaEntity user = springDataUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 유저입니다. userId=" + userId));
        user.assignBootcamp(bootcampId);
    }
}