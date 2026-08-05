package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.result.ChatChannelDetailResult;
import com.ohgiraffer.chat.application.result.ChatChannelListItemResult;
import com.ohgiraffer.chat.application.usecase.ChatChannelQueryUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChannelLastMessage;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/*
 * comment.
 *  ChatChannelQueryUseCase 구현체
 *  읽음 인원 계산: 채널 최신 메시지 id를 기준으로, 각 멤버의 lastReadMessageId가
 *  최신 메시지 id 이상이면 읽음으로 판단함 (최신 메시지가 없으면 전원 읽음)
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatChannelQueryService implements ChatChannelQueryUseCase {

    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    private final ChatMessageMirrorRepository chatMessageMirrorRepository;

    // 그룹 채팅방 상세 조회 - 참여자 목록 + 최신메시지 기준 읽음 인원 계산
    @Override
    public ChatChannelDetailResult getChannelDetail(String channelId) {
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // 최신 메시지 id 조회 - findByChannelIdOrderBySentAtDesc가 최신순 정렬이므로 첫 건이 최신 메시지
        List<ChatMessageMirror> messages = chatMessageMirrorRepository.findByChannelIdOrderBySentAtDesc(channelId);
        Long latestMessageId = messages.isEmpty() ? null : messages.get(0).getId();

        List<ChatChannelMember> chatChannelMembers =
                chatChannelMemberRepository.findAllByChatChannelIdAndLeftAtIsNull(channel.getId());

        List<ChatChannelDetailResult.ChatChannelMemberResult> members = chatChannelMembers.stream()
                .map(m -> new ChatChannelDetailResult.ChatChannelMemberResult(
                        m.getUserId(), m.getJoinedAt(), m.getLastReadMessageId(),
                        isRead(m.getLastReadMessageId(), latestMessageId)
                ))
                .toList();

        List<Long> readUserIds = members.stream()
                .filter(ChatChannelDetailResult.ChatChannelMemberResult::isRead)
                .map(ChatChannelDetailResult.ChatChannelMemberResult::userId)
                .toList();

        return new ChatChannelDetailResult(
                channel.getSendbirdChannelUrl(), channel.getName(), channel.getChannelType().name(),
                members, readUserIds.size(), readUserIds
        );
    }

    // 최신 메시지가 없으면(빈 채팅방) 전원 읽음, 있으면 lastReadMessageId가 최신 메시지 id 이상인지로 판단
    private boolean isRead(Long lastReadMessageId, Long latestMessageId) {
        if (latestMessageId == null) {
            return true;
        }
        return lastReadMessageId != null && lastReadMessageId >= latestMessageId;
    }

    // 참여 채팅방 목록 조회 - 멤버십/채널/최신메시지·안읽음수를 쿼리 3~4번으로 고정해서 조합
    @Override
    public List<ChatChannelListItemResult> getChannelList(Long userId, ChatChannel.ChannelType type) {
        // 1) 멤버십 - 채널 id 목록 확보용
        List<ChatChannelMember> memberships = chatChannelMemberRepository.findAllByUserIdAndLeftAtIsNull(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<Long> channelIds = memberships.stream().map(ChatChannelMember::getChatChannelId).toList();

        // 2) 채널 정보 일괄 조회
        List<ChatChannel> channels = chatChannelRepository.findAllByIdIn(channelIds);
        if (type != null) {
            channels = channels.stream().filter(c -> c.getChannelType() == type).toList();
        }

        List<String> sendbirdUrls = channels.stream().map(ChatChannel::getSendbirdChannelUrl).toList();

        // 3) 최신메시지(윈도우함수) + 안읽음수(조인집계) - 각각 쿼리 1번씩, DB에서 계산 끝냄
        Map<String, ChannelLastMessage> lastMessages = chatMessageMirrorRepository.findLatestMessagesByChannelIds(sendbirdUrls);
        Map<Long, Long> unreadCounts = chatChannelMemberRepository.findUnreadCountsByUserId(userId);

        return channels.stream()
                .map(channel -> {
                    ChannelLastMessage last = lastMessages.get(channel.getSendbirdChannelUrl());
                    return new ChatChannelListItemResult(
                            channel.getSendbirdChannelUrl(), channel.getName(), channel.getChannelType().name(),
                            last != null ? last.content() : null,
                            last != null ? last.sentAt() : null,
                            unreadCounts.getOrDefault(channel.getId(), 0L)
                    );
                })
                .toList();
    }

}
