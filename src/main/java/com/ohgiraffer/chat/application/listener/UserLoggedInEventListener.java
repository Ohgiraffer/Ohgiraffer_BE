package com.ohgiraffer.chat.application.listener;

import com.ohgiraffer.auth.domain.event.UserLoggedInEvent;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.global.s3.S3UrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * comment.
 *  로그인 성공 이벤트를 구독해서 Sendbird 유저 프로비저닝을 수행함
 *  일시적 장애(네트워크, Sendbird 5xx 등) 대비 최대 3회 재시도, 그래도 실패하면 recover에서 로그만 남기고 흡수
 *  -> 재시도로도 복구 안 되는 유저는 다음 로그인 시점에 이 리스너가 다시 타면서 자연 복구됨(프로비저닝은 멱등)
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class UserLoggedInEventListener {

    private final SendbirdApiPort sendbirdApiPort; // Sendbird 유저 생성/재사용
    private final S3UrlResolver s3UrlResolver;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("[UserLoggedInEventListener] 이벤트 수신 | userId={}", event.userId());
        try {
            String profileImgUrl = resolveProfileImgUrl(event.profileImg());
            sendbirdApiPort.provisionUser(event.userId(), event.name(), profileImgUrl);
            log.info("[UserLoggedInEventListener] 프로비저닝 성공 | userId={}", event.userId());
        } catch (Exception e) {
            log.warn("[UserLoggedInEventListener] Sendbird 유저 프로비저닝 실패 | userId={} | reason={}",
                    event.userId(), e.getMessage());
        }
    }

    private String resolveProfileImgUrl(String profileImgKey) {
        if (profileImgKey == null || profileImgKey.isBlank()) {
            return null;
        }
        return s3UrlResolver.resolve(profileImgKey);
    }

}
