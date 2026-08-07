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
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final UserRepository userRepository;

    // 그룹 채팅방 상세 조회 - 참여자 목록 + 최신메시지 기준 읽음 인원 계산
    @Override
    public ChatChannelDetailResult getChannelDetail(String channelId, Long principalId) {
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 채널 멤버가 아니면 존재 자체를 숨기고 404(CHAT_CHANNEL_NOT_FOUND)로 응답
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        // 최신 메시지 id 조회 - findByChannelIdOrderBySentAtDesc가 최신순 정렬이므로 첫 건이 최신 메시지
        Long latestMessageId = chatMessageMirrorRepository.findTopByChannelIdOrderBySentAtDesc(channelId)
                .map(ChatMessageMirror::getId)
                .orElse(null);

        List<ChatChannelMember> chatChannelMembers =
                chatChannelMemberRepository.findAllByChatChannelIdAndLeftAtIsNull(channel.getId());

        // 멤버 이름 벌크 조회 - 멤버마다 findById 반복 호출(N+1) 대신 id 모아서 한 번에 조회
        List<Long> memberUserIds = chatChannelMembers.stream()
                .map(ChatChannelMember::getUserId)
                .toList();
        Map<Long, String> memberNamesById = userRepository.findByIdIn(memberUserIds).stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user.getName()));

        List<ChatChannelDetailResult.ChatChannelMemberResult> members = chatChannelMembers.stream()
                .map(m -> new ChatChannelDetailResult.ChatChannelMemberResult(
                        m.getUserId(),
                        memberNamesById.get(m.getUserId()),
                        m.getJoinedAt(), m.getLastReadMessageId(),
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

    // 참여 채팅방 목록 조회 - 멤버십/채널/최신메시지·안읽음수를 쿼리 3~4번으로 고정해서 조합
    @Override
    public List<ChatChannelListItemResult> getChannelList(Long userId, ChatChannel.ChannelType type, String search) {
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

        // search가 있으면 채널 이름 부분검색으로 추가 필터 (대소문자/공백 무관)
        if (search != null && !search.isBlank()) {
            String keyword = normalizeForSearch(search);
            channels = channels.stream()
                    .filter(c -> c.getName() != null && normalizeForSearch(c.getName()).contains(keyword))
                    .toList();
        }

        List<String> sendbirdUrls = channels.stream().map(ChatChannel::getSendbirdChannelUrl).toList();

        // 3) 최신메시지(윈도우함수) + 안읽음수(조인집계) - 각각 쿼리 1번씩, DB에서 계산 끝냄

        if (sendbirdUrls.isEmpty()) {
            return List.of();
        }

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

    // 헤더 상시 노출용 - 전체 채널의 안읽은 메시지 합계만 가볍게 계산 (채널/최신메시지 조회 없이)
    @Override
    public long getTotalUnreadCount(Long userId) {
        Map<Long, Long> unreadCounts = chatChannelMemberRepository.findUnreadCountsByUserId(userId);
        return unreadCounts.values().stream().mapToLong(Long::longValue).sum();
    }

    // 검색어/채널명 비교용 정규화. 공백 제거 + 소문자 변환 (띄어쓰기 차이로 매칭 실패하는 것 방지)
    private String normalizeForSearch(String text) {
        return text.replaceAll("\\s+", "").toLowerCase();
    }

    // 최신 메시지가 없으면(빈 채팅방) 전원 읽음, 있으면 lastReadMessageId가 최신 메시지 id 이상인지로 판단
    private boolean isRead(Long lastReadMessageId, Long latestMessageId) {
        if (latestMessageId == null) {
            return true;
        }
        return lastReadMessageId != null && lastReadMessageId >= latestMessageId;
    }

}