package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.ReplyToMessageCommand;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.chat.application.usecase.ChatReplyCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
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
    // channelId 존재 검증용
    private final ChatChannelRepository chatChannelRepository;
    // 발신자 멤버십 검증용
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    // parentMessageId 실존/삭제여부 검증용
    private final ChatMessageMirrorRepository chatMessageMirrorRepository;

    // 스레드 답글 작성 - Sendbird 반영 성공 후 즉시 미러링 저장 (parentMessageId 채워서 답글로 저장됨)
    @Override
    @Transactional
    public SendbirdMessageResult reply(ReplyToMessageCommand command) {

        // channelId 실존 검증 + senderId 활성 멤버십 검증 (ChatMessageCommandService.sendMessage와 동일 패턴)
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(command.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), command.senderId())) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        // 원본 메시지를 sendbirdMessageId(String, 클라이언트 제공)로 조회 - 실존/삭제여부 검증
        ChatMessageMirror parentMessage = chatMessageMirrorRepository
                .findBySendbirdMessageId(command.parentSendbirdMessageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        // parentMessage가 요청한 channelId와 다른 채널 소속이면 차단
        // (다른 채널의 멤버십 검증을 우회해서 엉뚱한 채널 메시지에 답글 다는 것 방지)
        if (!parentMessage.getChannelId().equals(command.channelId())) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        if (parentMessage.isDeleted()) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_ALREADY_DELETED);
        }

        // Sendbird 호출에는 Sendbird 자체 숫자ID가 필요 - parentMessage에서 조회한 sendbirdMessageId를 Long으로 파싱
        Long sendbirdParentId = Long.parseLong(parentMessage.getSendbirdMessageId());

        SendbirdMessageResult result = sendbirdApiPort.sendReply(
                command.channelId(), sendbirdParentId, command.senderId(), command.content(), command.attachmentUrl()
        );

        // attachmentType(mime 타입)은 SendbirdMessageResult에 대응 필드가 없어 null 처리
        // 우리 DB 미러링에는 자기참조 FK로 내부 PK가 필요 - parentMessage.getId() 사용
        MirrorMessageCreatedCommand mirrorCommand = new MirrorMessageCreatedCommand(
                command.channelId(),
                result.sendbirdMessageId(),
                parentMessage.getId(),
                command.senderId(),
                command.content(),
                result.attachmentUrl(),
                null,
                result.sentAt()
        );
        chatMessageMirrorCommandUseCase.mirrorCreated(mirrorCommand);

        log.info("[Chat] 답글 전송 완료 | channelId={}, parentMessageId={}, messageId={}",
                command.channelId(), command.parentSendbirdMessageId(), result.sendbirdMessageId());

        return result;
    }

}
