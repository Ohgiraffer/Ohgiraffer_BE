package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  mirrorCreated 저장을 별도 물리 트랜잭션(REQUIRES_NEW)으로 분리하는 헬퍼
 *  - 같은 클래스 안에 REQUIRES_NEW 메서드를 두면 self-invocation이라 프록시가 안 먹혀서 별도 빈으로 분리함
 *  - 유니크 제약 위반이 나도 호출부(sendMessage/reply)의 바깥 트랜잭션까지 rollback-only로 오염되는 것을 막음
 */

@Component
@RequiredArgsConstructor
public class ChatMessageMirrorSaver {

    private final ChatMessageMirrorRepository chatMessageMirrorRepository;

    // saveAndFlush로 즉시 flush - 제약 위반 예외를 호출부의 catch 블록에서 그 자리에 바로 잡을 수 있게 함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAndFlush(ChatMessageMirror message) {
        chatMessageMirrorRepository.saveAndFlush(message);
    }

}
