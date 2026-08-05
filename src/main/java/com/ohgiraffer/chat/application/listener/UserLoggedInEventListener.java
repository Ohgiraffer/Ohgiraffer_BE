package com.ohgiraffer.chat.application.listener;

import com.ohgiraffer.auth.domain.event.UserLoggedInEvent;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserLoggedInEventListener {

    private final SendbirdApiPort sendbirdApiPort; // Sendbird 유저 생성/재사용

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("[UserLoggedInEventListener] 이벤트 수신 | userId={}", event.userId()); // 추가
        try {
            sendbirdApiPort.provisionUser(event.userId(), event.name(), event.profileImg());
            log.info("[UserLoggedInEventListener] 프로비저닝 성공 | userId={}", event.userId()); // 추가
        } catch (Exception e) {
            log.warn("[UserLoggedInEventListener] Sendbird 유저 프로비저닝 실패 | userId={} | reason={}",
                    event.userId(), e.getMessage());
        }
    }

}
