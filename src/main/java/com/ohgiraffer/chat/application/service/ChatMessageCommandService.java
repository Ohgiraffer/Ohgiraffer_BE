package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageDeletedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageUpdatedCommand;
import com.ohgiraffer.chat.application.usecase.ChatMessageCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  ChatMessageCommandUseCase 구현체
 *  웹훅 멱등성 처리: sendbird_message_id 기준으로 이미 미러링된 이벤트인지 확인 후 저장
 *  (같은 웹훅이 재전송되는 경우가 실제로 있어서, DB UNIQUE 제약 + 사전 체크 이중 방어)
 */

@Service
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final ChatMessageMirrorRepository repository;

    public ChatMessageCommandService(ChatMessageMirrorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void mirrorCreated(MirrorMessageCreatedCommand command) {
        if (repository.existsBySendbirdMessageId(command.sendbirdMessageId())) {
            return; // 이미 처리된 웹훅 이벤트 - 조용히 무시 (멱등성)
        }

        ChatMessageMirror message = ChatMessageMirror.create(
                command.channelId(),
                command.sendbirdMessageId(),
                command.parentMessageId(),
                command.senderId(),
                command.content(),
                command.attachmentUrl(),
                command.attachmentType(),
                command.sentAt()
        );
    }

    @Override
    @Transactional
    public void mirrorUpdated(MirrorMessageUpdatedCommand command) {
        ChatMessageMirror message = repository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        message.edit(command.content());
    }

    @Override
    @Transactional
    public void mirrorDeleted(MirrorMessageDeletedCommand command) {
        ChatMessageMirror message = repository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        message.delete();
    }

}
