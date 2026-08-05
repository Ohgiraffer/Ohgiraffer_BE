package com.ohgiraffer.notice.infrastructure.adapter;

import com.ohgiraffer.notice.application.port.AuthorNameQueryPort;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorNameQueryAdapter implements AuthorNameQueryPort {

    private final UserRepository userRepository;

    public AuthorNameQueryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<String> findName(Long authorId) {
        if (authorId == null) {
            return Optional.empty();
        }

        return userRepository.findById(authorId).map(User::getName);
    }

    @Override
    public Map<Long, String> findNames(Collection<Long> authorIds) {
        /*
         * 사용자 저장소에 목록 조회가 없어 식별자마다 한 번씩 묻는다.
         * 다만 공지 목록의 작성자는 강사·매니저 몇 명으로 겹치므로,
         * 중복을 먼저 걷어내면 질의 수가 공지 수가 아니라 사람 수만큼으로 줄어든다.
         *
         * 사용자 도메인에 목록 조회가 생기면 그 한 번으로 바꾼다.
         */
        Set<Long> distinctIds = authorIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> names = new HashMap<>();

        for (Long authorId : distinctIds) {
            userRepository.findById(authorId)
                    .ifPresent(user -> names.put(authorId, user.getName()));
        }

        return names;
    }
}
