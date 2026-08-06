package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.ReplyToMessageCommand;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.chat.application.usecase.ChatReplyCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  ChatReplyCommandUseCase 구현체
 *  Sendbird에 답글 반영 성공 후, 웹훅을 기다리지 않고 이 자리에서 바로 미러링 저장까지 처리함
 *  -> 웹훅이 뒤늦게 도착해도 existsBySendbirdMessageId()로 중복 저장 방지됨(멱등)
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatReplyCommandService implements ChatReplyCommandUseCase {

    private final SendbirdApiPort sendbirdApiPort;
    private final ChatMessageMirrorCommandUseCase chatMessageMirrorCommandUseCase;

    // 스레드 답글 작성 - Sendbird 반영 성공 후 즉시 미러링 저장 (parentMessageId 채워서 답글로 저장됨)
    @Override
    @Transactional
    public SendbirdMessageResult reply(ReplyToMessageCommand command) {
        SendbirdMessageResult result = sendbirdApiPort.sendReply(
                command.channelId(), command.parentMessageId(), command.senderId(), command.content(), command.attachmentUrl()
        );

        // attachmentType(mime 타입)은 SendbirdMessageResult에 대응 필드가 없어 null 처리 - 파일 첨부 답글 지원 시 재검토 필요
        MirrorMessageCreatedCommand mirrorCommand = new MirrorMessageCreatedCommand(
                command.channelId(),
                result.sendbirdMessageId(),
                command.parentMessageId(),
                command.senderId(),
                command.content(),
                result.attachmentUrl(),
                null,
                result.sentAt()
        );
        chatMessageMirrorCommandUseCase.mirrorCreated(mirrorCommand);

        log.info("[Chat] 답글 전송 완료 | channelId={}, parentMessageId={}, messageId={}",
                command.channelId(), command.parentMessageId(), result.sendbirdMessageId());

        return result;
    }

}
