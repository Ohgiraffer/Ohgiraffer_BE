package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.ProvisionChatUserCommand;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdUserProvisionResult;
import com.ohgiraffer.chat.application.usecase.ChatUserCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * comment.
 *  ChatUserCommandUseCase 구현체
 *  실제 프로비저닝 로직(생성 vs 재사용 판단)은 SendbirdApiAdapter가 담당,
 *  이 서비스는 usecase 계약과 port 호출만 연결함
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUserCommandService implements ChatUserCommandUseCase {

    private final SendbirdApiPort sendbirdApiPort;

    // 로그인 후 채팅 진입 시 Sendbird 유저 프로비저닝 - 이미 등록된 유저면 SendbirdApiAdapter가 알아서 토큰만 재발급
    @Override
    public SendbirdUserProvisionResult provisionUser(ProvisionChatUserCommand command) {
        SendbirdUserProvisionResult result = sendbirdApiPort.provisionUser(
                command.userId(),
                command.nickname(),
                command.profileUrl()
        );

        log.info("[Chat] Sendbird 유저 프로비저닝 완료 | userId={}, nickname={}",
                result.userId(), result.nickname());

        return result;
    }

}
