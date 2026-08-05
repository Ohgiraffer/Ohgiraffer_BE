package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdUserResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;
import com.ohgiraffer.chat.application.usecase.ChatUserQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * comment.
 *  ChatUserQueryUseCase 구현체
 *  SendbirdApiPort 호출만 연결 - 우리 DB 트랜잭션 불필요
 */

@Service
@RequiredArgsConstructor
public class ChatUserQueryService implements ChatUserQueryUseCase {

    private final SendbirdApiPort sendbirdApiPort;

    // 채팅 상대 검색 - 새채팅 모달 전용, Sendbird 유저 검색 그대로 위임
    @Override
    public List<SendbirdUserResult> searchUsers(String query) {
        return sendbirdApiPort.searchUsers(query);
    }

    // 온라인 상태 조회 - Sendbird 접속정보 그대로 위임
    @Override
    public SendbirdUserStatus getUserStatus(Long userId) {
        return sendbirdApiPort.getUserStatus(userId);
    }

}
