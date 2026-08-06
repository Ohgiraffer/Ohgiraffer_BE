package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageDeletedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageUpdatedCommand;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  ChatMessageMirrorCommandUseCase 구현체
 *  Sendbird 웹훅 이벤트를 받아서 chat_message_mirror 테이블에 반영함
 *  sendbird_message_id UNIQUE + existsBySendbirdMessageId()로 중복 이벤트(웹훅 재전송) 멱등 처리
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageMirrorCommandService implements ChatMessageMirrorCommandUseCase {

    private final ChatMessageMirrorRepository chatMessageMirrorRepository;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatMessageMirrorSaver chatMessageMirrorSaver;

    // 웹훅으로 수신한 메시지/답글 생성 이벤트 저장 - 중복 이벤트는 existsBySendbirdMessageId로 걸러냄
    // exists 체크 후 save 사이 경쟁상태 대비 - 유니크 제약 위반이면 이미 다른 요청이 저장한 것으로 보고 멱등 처리
    @Override
    @Transactional
    public void mirrorCreated(MirrorMessageCreatedCommand command) {
        // 채널이 우리 DB에 없으면(비정상 데이터거나 채널 생성 미러링이 아직 안 된 시점) 저장 안 함 - 존재하지 않는 채널 참조하는 orphan 메시지 방지
        if (chatChannelRepository.findBySendbirdChannelUrl(command.channelId()).isEmpty()) {
            log.warn("[Chat] 존재하지 않는 채널의 메시지 이벤트 - 스킵 | channelId={}, sendbirdMessageId={}",
                    command.channelId(), command.sendbirdMessageId());
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        if (chatMessageMirrorRepository.existsBySendbirdMessageId(command.sendbirdMessageId())) {
            log.info("[Chat] 이미 처리된 메시지 이벤트 - 스킵 | sendbirdMessageId={}", command.sendbirdMessageId());
            return;
        }

        try {
            ChatMessageMirror message = ChatMessageMirror.create(
                    command.channelId(), command.sendbirdMessageId(), command.parentMessageId(),
                    command.senderId(), command.content(), command.attachmentUrl(),
                    command.attachmentType(), command.sentAt()
            );
            chatMessageMirrorSaver.saveAndFlush(message); // REQUIRES_NEW + 즉시 flush

            log.info("[Chat] 메시지 미러링 완료 | channelId={}, sendbirdMessageId={}, parentMessageId={}",
                    command.channelId(), command.sendbirdMessageId(), command.parentMessageId());
        } catch (DataIntegrityViolationException e) {
            log.info("[Chat] 동시 저장 경쟁상태 감지 - 멱등 처리로 스킵 | sendbirdMessageId={}", command.sendbirdMessageId());
        }
    }

    // 웹훅으로 수신한 메시지/답글 수정 이벤트 반영 - sendbird_message_id로 기존 레코드 찾아서 content 갱신
    @Override
    @Transactional
    public void mirrorUpdated(MirrorMessageUpdatedCommand command) {
        ChatMessageMirror message = chatMessageMirrorRepository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        message.edit(command.content());
        chatMessageMirrorRepository.save(message);

        log.info("[Chat] 메시지 수정 미러링 완료 | sendbirdMessageId={}", command.sendbirdMessageId());
    }

    // 웹훅으로 수신한 메시지/답글 삭제 이벤트 반영 - 소프트 삭제(deletedAt 세팅)
    @Override
    @Transactional
    public void mirrorDeleted(MirrorMessageDeletedCommand command) {
        ChatMessageMirror message = chatMessageMirrorRepository.findBySendbirdMessageId(command.sendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        message.delete();
        chatMessageMirrorRepository.save(message);

        log.info("[Chat] 메시지 삭제 미러링 완료 | sendbirdMessageId={}", command.sendbirdMessageId());
    }

}
