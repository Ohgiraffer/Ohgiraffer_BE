package com.ohgiraffer.notice.infrastructure.adapter;

import com.ohgiraffer.notice.application.port.NoticeAudienceLookupPort;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * comment.
 *  NoticeAudienceLookupPort 구현체.
 *  UserRepository.findBootcampIdByUserId + findAllByBootcampId를 그대로 재사용함
 *  (둘 다 UserRepository에 이미 존재하는 메서드라 신규 쿼리 불필요).
 */
@Component
@RequiredArgsConstructor
public class NoticeAudienceLookupAdapter implements NoticeAudienceLookupPort {

    private final UserRepository userRepository;

    @Override
    public List<Long> findAllUserIdsInSameBootcamp(Long authorId) {
        Long bootcampId = userRepository.findBootcampIdByUserId(authorId)
                .orElse(null);

        if (bootcampId == null) {
            return List.of();
        }

        return userRepository.findAllByBootcampId(bootcampId).stream()
                .map(User::getId)
                .toList();
    }

}
