package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorQueryUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.ohgiraffer.chat.presentation.api.response.ReplyCountResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        Page<ChatMessageMirror> page = chatMessageMirrorRepository.findByChannelIdOrderBySentAtDesc(channelId, pageable);

        // 채널 활성 멤버 전원의 lastReadMessageId를 한 번에 조회 (쿼리 1번, 페이지 크기/멤버 수와 무관하게 고정)
        List<ChatChannelMember> members = chatChannelMemberRepository
                .findAllByChatChannelIdAndLeftAtIsNull(channel.getId());

        return page.map(message -> ChatMessageResult.from(message, calculateUnreadCount(message, members)));
    }

    // 스레드 답글 조회 - 부모 메시지가 속한 채널의 멤버십 검증 후 답글 목록 반환
    @Override
    public Page<ChatMessageResult> getThreadReplies(String parentSendbirdMessageId, Long principalId, Pageable pageable) {

        // 부모 메시지 조회 - 이 메시지가 속한 채널 기준으로 멤버십 검증할 것이므로 먼저 필요
        ChatMessageMirror parent = chatMessageMirrorRepository.findBySendbirdMessageId(parentSendbirdMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(parent.getChannelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 원본 메시지가 속한 채널의 활성 멤버인지 검증
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        // 답글 저장 시 자기참조 FK로 내부 PK를 썼으므로, 조회도 내부 PK(parent.getId()) 기준
        return chatMessageMirrorRepository.findByParentMessageId(parent.getId(), pageable)
                .map(ChatMessageResult::from);

    }

    // 메시지 통합 검색 - channelId 지정 시 해당 채널 멤버십 검증, 미지정 시 내 채널로만 범위 강제 제한
    // 결과에 여러 채널이 섞일 수 있어, 등장하는 채널/멤버 정보를 각각 쿼리 1번씩 벌크 조회해서 계산 (N+1 방지)
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

        Page<ChatMessageMirror> page = chatMessageMirrorRepository.search(condition, pageable);

        // 페이지에 실제로 등장한 채널 URL만 추출 (검색 조건 채널 목록이 아니라, 이번 페이지 결과 기준)
        List<String> channelUrlsInPage = page.getContent().stream()
                .map(ChatMessageMirror::getChannelId)
                .distinct()
                .toList();

        if (channelUrlsInPage.isEmpty()) {
            return page.map(message -> ChatMessageResult.from(message, null));
        }

        // 채널 벌크 조회 (쿼리 1번)
        Map<String, ChatChannel> channelsByUrl = chatChannelRepository.findAllBySendbirdChannelUrlIn(channelUrlsInPage).stream()
                .collect(Collectors.toMap(ChatChannel::getSendbirdChannelUrl, c -> c));

        List<Long> channelIds = channelsByUrl.values().stream().map(ChatChannel::getId).toList();

        // 멤버 벌크 조회 (쿼리 1번) - 이미 있는 findAllByChatChannelIdInAndLeftAtIsNull 재사용
        List<ChatChannelMember> allMembers = chatChannelMemberRepository
                .findAllByChatChannelIdInAndLeftAtIsNull(channelIds);

        // 채널(내부 PK)별로 멤버 목록 그룹핑 - 메모리 연산, 추가 쿼리 없음
        Map<Long, List<ChatChannelMember>> membersByChannelId = allMembers.stream()
                .collect(Collectors.groupingBy(ChatChannelMember::getChatChannelId));

        return page.map(message -> {
            ChatChannel channel = channelsByUrl.get(message.getChannelId());
            List<ChatChannelMember> members = channel != null
                    ? membersByChannelId.getOrDefault(channel.getId(), List.of())
                    : List.of();
            return ChatMessageResult.from(message, calculateUnreadCount(message, members));
        });

    }

    // 메시지 하나의 안읽음 수 계산 - 발신자 본인을 제외한 활성 멤버 중 아직 이 메시지를 안 읽은 인원 수
    // (lastReadMessageId가 이 메시지 id 미만이거나 null인 멤버 = 안 읽음)
    private Long calculateUnreadCount(ChatMessageMirror message, List<ChatChannelMember> members) {
        return members.stream()
                .filter(m -> !m.getUserId().equals(message.getSenderId())) // 발신자 본인 제외
                .filter(m -> m.getLastReadMessageId() == null || m.getLastReadMessageId() < message.getId())
                .count();
    }

    // 특정 메시지의 답글 개수 조회
    // messageId는 클라이언트가 갖고 있는 sendbirdMessageId(String)이지만,
    // 실제 parentMessageId 저장/조회는 내부 PK(Long) 기준이라 원본 메시지를 먼저 찾아 PK로 변환해야 함
    // (getThreadReplies와 동일한 principalId 멤버십 검증 패턴 적용)
    @Override
    public ReplyCountResponse getReplyCount(String messageId, Long principalId) {
        ChatMessageMirror parent = chatMessageMirrorRepository.findBySendbirdMessageId(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(parent.getChannelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 원본 메시지가 속한 채널의 활성 멤버인지 검증
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principalId)) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        long count = chatMessageMirrorRepository.countByParentMessageIdAndDeletedAtIsNull(parent.getId());
        return new ReplyCountResponse(messageId, count);
    }

}
