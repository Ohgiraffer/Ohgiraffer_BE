package com.ohgiraffer.approval.infrastructure.adapter;

import com.ohgiraffer.approval.application.port.GetBootcampManagerIdsPort;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * comment.
 *  GetBootcampManagerIdsPort 구현체.
 *  UserNotificationSettingAdapter와 동일 패턴 - 같은 DB를 쓰는 모놀리식 구조라
 *  user 도메인의 domain 계층 UserRepository를 직접 참조함 (JPA 세부구현 아닌 domain 인터페이스 의존).
 *  UserRepository에 이미 findIdsByBootcampIdAndRole이 있어 그대로 위임만 함.
 */

@Component
@RequiredArgsConstructor
public class GetBootcampManagerIdsAdapter implements GetBootcampManagerIdsPort {

    private final UserRepository userRepository;

    @Override
    public List<Long> findManagerIdsByBootcampId(Long bootcampId) {
        return userRepository.findIdsByBootcampIdAndRole(bootcampId, Role.MANAGER);
    }

}
