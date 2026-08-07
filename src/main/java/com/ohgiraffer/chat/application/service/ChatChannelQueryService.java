package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.ChatChannelDetailResult;
import com.ohgiraffer.chat.application.result.ChatChannelListItemResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;
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
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * comment.
 *  ChatChannelQueryUseCase 구현체
 *  읽음 인원 계산: 채널 최신 메시지 id를 기준으로, 각 멤버의 lastReadMessageId가
 *  최신 메시지 id 이상이면 읽음으로 판단함 (최신 메시지가 없으면 전원 읽음)
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatChannelQueryService implements ChatChannelQueryUseCase {

    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    private final ChatMessageMirrorRepository chatMessageMirrorRepository;
    private final UserRepository userRepository;
    private final SendbirdApiPort sendbirdApiPort;

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
        Map<Long, User> usersById = userRepository.findByIdIn(memberUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<ChatChannelDetailResult.ChatChannelMemberResult> members = chatChannelMembers.stream()
                .map(m -> {
                    User user = usersById.get(m.getUserId());
                    return new ChatChannelDetailResult.ChatChannelMemberResult(
                            m.getUserId(),
                            user != null ? user.getName() : null,
                            user != null ? user.getEmail() : null,
                            user != null ? user.getRole().name() : null,
                            m.getJoinedAt(), m.getLastReadMessageId(),
                            isRead(m.getLastReadMessageId(), latestMessageId)
                    );
                })
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
        Map<String, Boolean> onlineByChannelUrl = buildOnlineStatusForDmChannels(channels, userId);

        return channels.stream()
                .map(channel -> {
                    ChannelLastMessage last = lastMessages.get(channel.getSendbirdChannelUrl());
                    return new ChatChannelListItemResult(
                            channel.getSendbirdChannelUrl(), channel.getName(), channel.getChannelType().name(),
                            last != null ? last.content() : null,
                            last != null ? last.sentAt() : null,
                            unreadCounts.getOrDefault(channel.getId(), 0L),
                            onlineByChannelUrl.get(channel.getSendbirdChannelUrl()) // DM 아니면 null
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

    // DM 채널만 골라 상대방(나 아닌 멤버) userId를 찾고, Sendbird 온라인 상태를 배치 조회
    // GROUP 채널은 대상에서 제외 (목록 화면에서 굳이 온라인 표시 안 함)
    private Map<String, Boolean> buildOnlineStatusForDmChannels(List<ChatChannel> channels, Long userId) {
        List<ChatChannel> dmChannels = channels.stream()
                .filter(c -> c.getChannelType() == ChatChannel.ChannelType.DM)
                .toList();
        if (dmChannels.isEmpty()) {
            return Map.of();
        }

        List<Long> dmChannelIds = dmChannels.stream().map(ChatChannel::getId).toList();
        List<ChatChannelMember> dmMembers = chatChannelMemberRepository.findAllByChatChannelIdInAndLeftAtIsNull(dmChannelIds);

        // 채널별로 "나 아닌 멤버"(상대방) userId 매핑
        Map<Long, Long> otherUserIdByChannelId = dmMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .collect(Collectors.toMap(ChatChannelMember::getChatChannelId, ChatChannelMember::getUserId, (a, b) -> a));

        List<Long> otherUserIds = otherUserIdByChannelId.values().stream().distinct().toList();
        Map<Long, Boolean> onlineByUserId = fetchOnlineStatuses(otherUserIds);

        Map<Long, String> channelIdToUrl = dmChannels.stream()
                .collect(Collectors.toMap(ChatChannel::getId, ChatChannel::getSendbirdChannelUrl));

        Map<String, Boolean> result = new HashMap<>();
        otherUserIdByChannelId.forEach((chatChannelId, otherUserId) -> {
            String url = channelIdToUrl.get(chatChannelId);
            if (url != null) {
                result.put(url, onlineByUserId.getOrDefault(otherUserId, false));
            }
        });
        return result;
    }

    // Sendbird 온라인 상태 일괄 조회 - 실패하면 전원 offline으로 기본 처리 (목록 조회 자체를 막지 않음)
    private Map<Long, Boolean> fetchOnlineStatuses(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            return sendbirdApiPort.getUserStatuses(userIds).stream()
                    .collect(Collectors.toMap(SendbirdUserStatus::userId, SendbirdUserStatus::isOnline));
        } catch (BusinessException e) {
            log.warn("[Chat] DM 온라인 상태 일괄 조회 실패 - 전원 offline으로 처리 | userIds={}", userIds);
            return Map.of();
        }
    }

    // 검색어/채널명 비교용 정규화. 공백 제거 + 소문자 변환(Locale.ROOT로 JVM 기본 로케일 영향 배제, 터키어 로케일 등에서 i/I 변환 오류 방지)
    private String normalizeForSearch(String text) {
        return text.replaceAll("\\s+", "").toLowerCase(java.util.Locale.ROOT);
    }

    // 최신 메시지가 없으면(빈 채팅방) 전원 읽음, 있으면 lastReadMessageId가 최신 메시지 id 이상인지로 판단
    private boolean isRead(Long lastReadMessageId, Long latestMessageId) {
        if (latestMessageId == null) {
            return true;
        }
        return lastReadMessageId != null && lastReadMessageId >= latestMessageId;
    }

}