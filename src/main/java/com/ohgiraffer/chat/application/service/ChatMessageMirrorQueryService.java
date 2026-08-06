package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorQueryUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  ChatMessageQueryUseCase 구현체
 *  조회 전용이라 @Transactional(readOnly = true)로 처리
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageMirrorQueryService implements ChatMessageMirrorQueryUseCase {

    private final ChatMessageMirrorRepository chatMessageMirrorRepository;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    //  채널 메시지 이력 조회 - principalId로 채널 멤버십 검증 후 최신순 정렬 리스트 반환
    @Override
    public Page<ChatMessageResult> getChannelMessages(String channelId, Long principalId, Pageable pageable) {

        // channelId(Sendbird url)로 우리 DB 채널 조회 - 없으면 404
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 호출자가 이 채널의 활성 멤버인지 검증
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        return chatMessageMirrorRepository.findByChannelIdOrderBySentAtDesc(channelId, pageable)
                .map(ChatMessageResult::from);
    }

    // 스레드 답글 조회 - 부모 메시지가 속한 채널의 멤버십 검증 후 답글 목록 반환
    @Override
    public Page<ChatMessageResult> getThreadReplies(Long parentMessageId, Long principalId, Pageable pageable) {

        // 부모 메시지 조회 - 이 메시지가 속한 채널 기준으로 멤버십 검증할 것이므로 먼저 필요
        ChatMessageMirror parent = chatMessageMirrorRepository.findById(parentMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(parent.getChannelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 원본 메시지가 속한 채널의 활성 멤버인지 검증
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        return chatMessageMirrorRepository.findByParentMessageId(parentMessageId, pageable)
                .map(ChatMessageResult::from);
    }

    // 메시지 통합 검색 - channelId 지정 시 해당 채널 멤버십 검증, 미지정 시 내 채널로만 범위 강제 제한
    @Override
    public Page<ChatMessageResult> searchMessages(ChatMessageSearchCondition condition, Long principalId, Pageable pageable) {

        if (condition.channelId() != null) {
            // 특정 채널 검색 - 그 채널 멤버십만 검증
            ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(condition.channelId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));
            if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
                throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
            }
        } else {
            // channelId 미지정 - 전체 검색 대신 내가 속한 채널로만 검색 범위를 강제 제한 (IDOR 방지)
            List<String> myChannelUrls = chatChannelMemberRepository.findAllByUserIdAndLeftAtIsNull(principalId).stream()
                    .map(m -> chatChannelRepository.findById(m.getChatChannelId()))
                    .flatMap(Optional::stream)
                    .map(ChatChannel::getSendbirdChannelUrl)
                    .toList();
            condition = new ChatMessageSearchCondition(condition, myChannelUrls);
        }

        return chatMessageMirrorRepository.search(condition, pageable).map(ChatMessageResult::from);
    }

}
