package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.*;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageCommandUseCase;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  ChatMessageCommandUseCase 구현체
 *  Sendbird에 먼저 반영 성공 후, 웹훅을 기다리지 않고 이 자리에서 바로 미러링 처리(ChatReplyCommandService와 동일 패턴)
 *  수정/삭제는 본인이 작성한 메시지인지 chat_message_mirror 기준으로 검증 후 처리
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final SendbirdApiPort sendbirdApiPort;
    private final ChatMessageMirrorRepository chatMessageMirrorRepository;
    private final ChatMessageMirrorCommandUseCase chatMessageMirrorCommandUseCase;

    // 메시지 전송 - Sendbird 반영 성공 후 즉시 미러링 저장 (parentMessageId=null이라 일반 메시지로 저장됨)
    @Override
    @Transactional
    public SendbirdMessageResult sendMessage(SendMessageCommand command) {
        SendbirdMessageResult result = sendbirdApiPort.sendMessage(
                command.channelId(), command.senderId(), command.content(),
                command.attachmentUrl(), command.mentionedUserIds()
        );

        // attachmentType(mime 타입)은 SendbirdMessageResult에 대응 필드가 없어 null 처리
        chatMessageMirrorCommandUseCase.mirrorCreated(new MirrorMessageCreatedCommand(
                command.channelId(), result.sendbirdMessageId(), null, command.senderId(),
                command.content(), result.attachmentUrl(), null, result.sentAt()
        ));

        log.info("[Chat] 메시지 전송 완료 | channelId={}, messageId={}", command.channelId(), result.sendbirdMessageId());

        return result;
    }

    // 메시지/답글 수정 - 본인 확인 + 이미 삭제된 메시지인지 검증 후 Sendbird 반영, 성공하면 미러링도 갱신
    @Override
    @Transactional
    public void updateMessage(UpdateMessageCommand command) {
        ChatMessageMirror message = chatMessageMirrorRepository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        // 본인이 작성한 메시지인지 확인
        if (!message.getSenderId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_ACCESS_DENIED);
        }
        // 이미 삭제된 메시지는 수정 불가
        if (message.isDeleted()) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_ALREADY_DELETED);
        }

        sendbirdApiPort.updateMessage(command.channelId(), command.sendbirdMessageId(), command.content());

        chatMessageMirrorCommandUseCase.mirrorUpdated(
                new MirrorMessageUpdatedCommand(command.sendbirdMessageId(), command.content())
        );

        log.info("[Chat] 메시지 수정 완료 | channelId={}, messageId={}", command.channelId(), command.sendbirdMessageId());
    }

    // 메시지/답글 삭제 - 본인 확인 + 중복 삭제 방지 검증 후 Sendbird 반영, 성공하면 미러링도 소프트삭제
    @Override
    @Transactional
    public void deleteMessage(DeleteMessageCommand command) {
        ChatMessageMirror message = chatMessageMirrorRepository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        // 본인이 작성한 메시지인지 확인
        if (!message.getSenderId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_ACCESS_DENIED);
        }
        // 이미 삭제된 메시지는 재삭제 불가
        if (message.isDeleted()) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_ALREADY_DELETED);
        }

        sendbirdApiPort.deleteMessage(command.channelId(), command.sendbirdMessageId());

        chatMessageMirrorCommandUseCase.mirrorDeleted(
                new MirrorMessageDeletedCommand(command.sendbirdMessageId())
        );

        log.info("[Chat] 메시지 삭제 완료 | channelId={}, messageId={}", command.channelId(), command.sendbirdMessageId());
    }

}
