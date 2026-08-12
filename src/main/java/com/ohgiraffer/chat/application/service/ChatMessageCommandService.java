package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.*;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageCommandUseCase;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
    // channelId 존재 검증용
    private final ChatChannelRepository chatChannelRepository;
    // 발신자 멤버십 검증용
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    // mentionedUserIds 실존 검증용
    private final UserRepository userRepository;


    // 메시지 전송 - Sendbird 반영 성공 후 즉시 미러링 저장 (parentMessageId=null이라 일반 메시지로 저장됨)
    @Override
    @Transactional
    public SendbirdMessageResult sendMessage(SendMessageCommand command) {

        // channelId 실존 검증 + senderId 활성 멤버십 검증 (IDOR 방지, ChatAttachmentController와 동일 패턴)
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(command.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), command.senderId())) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        // 멘션 대상이 이 채널의 활성 멤버인지 검증 (유저 실존 여부와 별개)
        validateMentionedUsersAreChannelMembers(channel.getId(), command.mentionedUserIds());

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

    // 멘션 대상이 해당 채널 멤버가 아니면 차단
    private void validateMentionedUsersAreChannelMembers(Long chatChannelId, List<Long> mentionedUserIds) {
        if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return;
        }
        List<Long> notMembers = mentionedUserIds.stream()
                .filter(userId -> !chatChannelMemberRepository.existsActiveMembership(chatChannelId, userId))
                .toList();

        if (!notMembers.isEmpty()) {
            log.warn("[Chat] 채널 멤버가 아닌 유저 멘션 시도 | notMembers={}", notMembers);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "채널 멤버만 멘션할 수 있습니다.");
        }
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

        // attachmentUrl 정규화. 빈 문자열/"null" 같은 무의미한 값은 "제거 의도"로 통일
        String normalizedContent = (command.content() == null || command.content().isBlank())
                ? null : command.content();
        String normalizedUrl = normalizeAttachmentUrl(command.attachmentUrl());

        // 원래 메시지 타입 판단 - 기존 attachmentUrl 존재 여부 기준
        boolean wasFile = message.getAttachmentUrl() != null;
        String messageType = wasFile ? "FILE" : "MESG";

        // FILE 메시지는 텍스트 수정 불가 (Sendbird가 실제로 반영을 안 해줌)
        if (wasFile && normalizedContent != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "첨부파일이 있는 메시지는 텍스트를 수정할 수 없습니다. 첨부파일만 교체 가능합니다.");
        }

        // MESG였던 메시지에 새 첨부파일을 붙이려는 시도는 차단 (Sendbird가 타입 전환 자체를 허용 안 함)
        if (!wasFile && normalizedUrl != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "첨부파일이 없던 메시지에는 첨부파일을 추가할 수 없습니다. 새 메시지로 보내주세요.");
        }

        if (normalizedContent == null && normalizedUrl == null && !wasFile) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수정할 내용이 필요합니다.");
        }

        // FILE 메시지면 새 url이 있으면 교체, 없으면 기존 url 그대로 유지 (Sendbird가 매번 url을 요구하므로)
        String effectiveUrl = wasFile
                ? (normalizedUrl != null ? normalizedUrl : message.getAttachmentUrl())
                : null;

        sendbirdApiPort.updateMessage(command.channelId(), command.sendbirdMessageId(), messageType, normalizedContent, effectiveUrl);

        // 사용자 직접 수정은 Sendbird API 호출이 성공한 이 시점 자체가 "이벤트 발생 시각"임 -
        // 어떤 지연된 웹훅보다도 항상 최신으로 취급되어야 하므로 Instant.now() 사용
        chatMessageMirrorCommandUseCase.mirrorUpdated(
                new MirrorMessageUpdatedCommand(command.sendbirdMessageId(), normalizedContent, effectiveUrl, Instant.now())
        );

        log.info("[Chat] 메시지 수정 완료 | channelId={}, messageId={}", command.channelId(), command.sendbirdMessageId());
    }

    // null/blank/"null" 문자열/host 없는 반쪽 URL(예: "https://")은 전부 null(첨부파일 없음/제거)로 통일
    private String normalizeAttachmentUrl(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        try {
            java.net.URI uri = new java.net.URI(attachmentUrl);
            boolean validScheme = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            boolean validHost = uri.getHost() != null && !uri.getHost().isBlank();
            return (validScheme && validHost) ? attachmentUrl : null;
        } catch (java.net.URISyntaxException e) {
            return null;
        }
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
                new MirrorMessageDeletedCommand(command.sendbirdMessageId(), Instant.now())
        );

        log.info("[Chat] 메시지 삭제 완료 | channelId={}, messageId={}", command.channelId(), command.sendbirdMessageId());
    }

}
