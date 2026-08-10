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
import java.util.Objects;
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
                            user != null ? user.getProfileImg() : null,
                            m.getJoinedAt(), m.getLastReadMessageId(),
                            isRead(m.getLastReadMessageId(), latestMessageId)
                    );
                })
                .toList();

        List<Long> readUserIds = members.stream()
                .filter(ChatChannelDetailResult.ChatChannelMemberResult::isRead)
                .map(ChatChannelDetailResult.ChatChannelMemberResult::userId)
                .toList();

        // 채널 이름 미지정(null/공백)이면 본인 제외 참여자 이름으로 대체 (DM/GROUP 공용 규칙)
        String displayName = channel.getName();
        if (displayName == null || displayName.isBlank()) {
            List<String> otherMemberNames = chatChannelMembers.stream()
                    .filter(m -> !m.getUserId().equals(principalId))
                    .map(m -> usersById.get(m.getUserId()))
                    .filter(Objects::nonNull)
                    .map(User::getName)
                    .toList();
            displayName = buildDisplayNameFromMembers(otherMemberNames);
        }

        return new ChatChannelDetailResult(
                channel.getSendbirdChannelUrl(), displayName, channel.getChannelType().name(),
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
        // 이름 미지정 DM 채널(name=null)은 검색어가 있으면 매칭 대상에서 자연히 제외됨 (원한다면 상대방 이름 기준 검색 추가 필요 - 별도 확인 필요)
        if (search != null && !search.isBlank()) {
            String keyword = normalizeForSearch(search);
            channels = channels.stream()
                    .filter(c -> c.getName() != null && normalizeForSearch(c.getName()).contains(keyword))
                    .toList();
        }

        List<String> sendbirdUrls = channels.stream().map(ChatChannel::getSendbirdChannelUrl).toList();

        if (sendbirdUrls.isEmpty()) {
            return List.of();
        }

        // 3) 최신메시지(윈도우함수) + 안읽음수(조인집계) - 각각 쿼리 1번씩, DB에서 계산 끝냄
        Map<String, ChannelLastMessage> lastMessages = chatMessageMirrorRepository.findLatestMessagesByChannelIds(sendbirdUrls);
        Map<Long, Long> unreadCounts = chatChannelMemberRepository.findUnreadCountsByUserId(userId);

        // 4) DM 채널의 상대방 정보(온라인 상태/프로필/이름) - 한 번에 조회
        DmPartnerInfo dmPartnerInfo = buildDmPartnerInfo(channels, userId);

        // 5) 이름 미지정 채널(DM/GROUP 공용) 표시명 - 본인 제외 참여자 이름으로 대체
        Map<String, String> generatedNameByChannelUrl = buildDisplayNamesForNamelessChannels(channels, userId);

        return channels.stream()
                .map(channel -> {
                    String url = channel.getSendbirdChannelUrl();
                    ChannelLastMessage last = lastMessages.get(url);

                    // 채널에 이름이 지정되어 있으면 그대로, 미지정이면 참여자 이름으로 대체
                    String displayName = (channel.getName() != null && !channel.getName().isBlank())
                            ? channel.getName()
                            : generatedNameByChannelUrl.get(url);

                    return new ChatChannelListItemResult(
                            url, displayName, channel.getChannelType().name(),
                            last != null ? last.content() : null,
                            last != null ? last.sentAt() : null,
                            unreadCounts.getOrDefault(channel.getId(), 0L),
                            dmPartnerInfo.profileUrlByChannelUrl().get(url), // GROUP이면 null
                            dmPartnerInfo.onlineByChannelUrl().get(url) // GROUP이면 null
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
    private DmPartnerInfo buildDmPartnerInfo(List<ChatChannel> channels, Long userId) {
        List<ChatChannel> dmChannels = channels.stream()
                .filter(c -> c.getChannelType() == ChatChannel.ChannelType.DM)
                .toList();
        if (dmChannels.isEmpty()) {
            return new DmPartnerInfo(Map.of(), Map.of());
        }

        List<Long> dmChannelIds = dmChannels.stream().map(ChatChannel::getId).toList();
        List<ChatChannelMember> dmMembers = chatChannelMemberRepository.findAllByChatChannelIdInAndLeftAtIsNull(dmChannelIds);

        // 채널별로 "나 아닌 멤버"(상대방) userId 매핑
        Map<Long, Long> otherUserIdByChannelId = dmMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .collect(Collectors.toMap(ChatChannelMember::getChatChannelId, ChatChannelMember::getUserId, (a, b) -> a));

        List<Long> otherUserIds = otherUserIdByChannelId.values().stream().distinct().toList();

        // 온라인 상태는 Sendbird에서, 이름/프로필은 자체 User 테이블에서 각각 벌크 조회
        Map<Long, Boolean> onlineByUserId = fetchOnlineStatuses(otherUserIds);
        Map<Long, User> usersByOtherUserId = userRepository.findByIdIn(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, String> channelIdToUrl = dmChannels.stream()
                .collect(Collectors.toMap(ChatChannel::getId, ChatChannel::getSendbirdChannelUrl));

        Map<String, Boolean> onlineByChannelUrl = new HashMap<>();
        Map<String, String> profileUrlByChannelUrl = new HashMap<>();

        otherUserIdByChannelId.forEach((chatChannelId, otherUserId) -> {
            String url = channelIdToUrl.get(chatChannelId);
            if (url == null) {
                return;
            }
            onlineByChannelUrl.put(url, onlineByUserId.getOrDefault(otherUserId, false));
            User partner = usersByOtherUserId.get(otherUserId);
            if (partner != null) {
                profileUrlByChannelUrl.put(url, partner.getProfileImg());
            }
        });

        return new DmPartnerInfo(onlineByChannelUrl, profileUrlByChannelUrl);
    }

    // DM 채널의 상대방 관련 정보 묶음 - 온라인 상태 / 프로필 URL
    private record DmPartnerInfo(
            Map<String, Boolean> onlineByChannelUrl,
            Map<String, String> profileUrlByChannelUrl

    ) {
    }


    // 이름 미지정(null/공백) 채널(DM+GROUP 공용) 대상으로, 본인 제외 참여자 이름을 조합한 표시명을 벌크로 계산
    // 쿼리 1번(멤버 조회) + User 벌크 조회 1번으로 고정 - 미지정 채널 수와 무관
    private Map<String, String> buildDisplayNamesForNamelessChannels(List<ChatChannel> channels, Long userId) {
        List<ChatChannel> namelessChannels = channels.stream()
                .filter(c -> c.getName() == null || c.getName().isBlank())
                .toList();
        if (namelessChannels.isEmpty()) {
            return Map.of();
        }

        List<Long> namelessChannelIds = namelessChannels.stream().map(ChatChannel::getId).toList();
        List<ChatChannelMember> members = chatChannelMemberRepository
                .findAllByChatChannelIdInAndLeftAtIsNull(namelessChannelIds);

        // 채널별로 "나 아닌 멤버" userId 목록 매핑
        Map<Long, List<Long>> otherUserIdsByChannelId = members.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .collect(Collectors.groupingBy(ChatChannelMember::getChatChannelId,
                        Collectors.mapping(ChatChannelMember::getUserId, Collectors.toList())));

        List<Long> allOtherUserIds = otherUserIdsByChannelId.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        Map<Long, User> usersById = userRepository.findByIdIn(allOtherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, String> channelIdToUrl = namelessChannels.stream()
                .collect(Collectors.toMap(ChatChannel::getId, ChatChannel::getSendbirdChannelUrl));

        Map<String, String> result = new HashMap<>();
        for (ChatChannel channel : namelessChannels) {
            String url = channelIdToUrl.get(channel.getId());
            List<Long> otherIds = otherUserIdsByChannelId.getOrDefault(channel.getId(), List.of());
            List<String> otherNames = otherIds.stream()
                    .map(usersById::get)
                    .filter(Objects::nonNull)
                    .map(User::getName)
                    .toList();
            result.put(url, buildDisplayNameFromMembers(otherNames));
        }
        return result;
    }

    // 참여자 이름 목록으로 표시명 조합 - 3명 이하면 전부 나열, 4명 이상이면 앞 2명 + "외 N명"으로 축약
    // (DM은 항상 1명이라 이름 그대로 반환됨)
    private String buildDisplayNameFromMembers(List<String> otherMemberNames) {
        if (otherMemberNames.isEmpty()) {
            return null;
        }
        if (otherMemberNames.size() <= 3) {
            return String.join(", ", otherMemberNames);
        }
        String prefix = String.join(", ", otherMemberNames.subList(0, 2));
        int remaining = otherMemberNames.size() - 2;
        return prefix + " 외 " + remaining + "명";
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