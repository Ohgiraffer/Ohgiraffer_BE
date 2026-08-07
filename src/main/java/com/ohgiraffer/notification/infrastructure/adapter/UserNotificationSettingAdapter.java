package com.ohgiraffer.notification.infrastructure.adapter;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notification.application.port.UserNotificationSettingPort;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  UserNotificationSettingPort 구현체
 *  같은 DB를 쓰는 모놀리식 구조라 user 도메인의 domain 계층 UserRepository를 직접 참조
 *  (JPA/infra 세부구현이 아닌 domain 인터페이스 의존이라 클린아키텍처 위반 아님, ChatMessageCommandService의 멘션 검증과 동일 패턴)
 */

@Component
@RequiredArgsConstructor
public class UserNotificationSettingAdapter implements UserNotificationSettingPort {

    private final UserRepository userRepository;

    // 해당 유저가 실시간 알림 수신을 켜뒀는지 여부 - user 없으면 USER_NOT_FOUND
    @Override
    public boolean isNotificationOn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return user.isNotificationOn();
    }

}
