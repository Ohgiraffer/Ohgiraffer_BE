package com.ohgiraffer.chat.presentation.api.controller;

import com.ohgiraffer.chat.application.command.*;
import com.ohgiraffer.chat.application.result.ChatChannelDetailResult;
import com.ohgiraffer.chat.application.result.ChatChannelResult;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;
import com.ohgiraffer.chat.application.usecase.*;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.ohgiraffer.chat.presentation.api.request.*;
import com.ohgiraffer.chat.presentation.api.response.*;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatChannelCommandUseCase chatChannelCommandUseCase;
    private final ChatChannelQueryUseCase chatChannelQueryUseCase;
    private final ChatMessageCommandUseCase chatMessageCommandUseCase;
    private final ChatMessageMirrorQueryUseCase chatMessageMirrorQueryUseCase;
    private final ChatReplyCommandUseCase chatReplyCommandUseCase;
    private final ChatUserQueryUseCase chatUserQueryUseCase;

    // 채팅방 생성
    @PostMapping("/channels")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody CreateChannelRequest request) {
        ChatChannelResult result = chatChannelCommandUseCase.createChannel(
                new CreateChannelCommand(request.userIds(), request.name())
        );
        return ResponseEntity.ok(ChannelResponse.from(result));
    }

    // 참여 채팅방 목록 조회 - type 없으면 전체, direct/group이면 필터
    @GetMapping("/channels")
    public ResponseEntity<List<ChatChannelListItemResponse>> getChannelList(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String type
    ) {
        ChatChannel.ChannelType channelType = type == null ? null
                : ChatChannel.ChannelType.valueOf(type.toUpperCase());

        List<ChatChannelListItemResponse> result = chatChannelQueryUseCase
                .getChannelList(principal.getId(), channelType)
                .stream()
                .map(ChatChannelListItemResponse::from)
                .toList();

        return ResponseEntity.ok(result);
    }

    // 메시지 전송(파일·멘션 포함)
    @PostMapping("/channels/{channelId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String channelId,
            @RequestBody SendMessageRequest request
    ) {

        SendbirdMessageResult result = chatMessageCommandUseCase.sendMessage(
                new SendMessageCommand(channelId, principal.getId(), request.content(),
                        request.attachmentUrl(), request.mentionedUserIds())
        );
        return ResponseEntity.ok(ChatMessageResponse.from(result));
    }

    // 스레드 답글 작성
    @PostMapping("/messages/{messageId}/replies")
    public ResponseEntity<ChatMessageResponse> reply(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long messageId,
            @RequestBody ReplyRequest request
    ) {
        SendbirdMessageResult result = chatReplyCommandUseCase.reply(
                new ReplyToMessageCommand(request.channelId(), messageId, principal.getId(),
                        request.content(), request.attachmentUrl())
        );
        return ResponseEntity.ok(ChatMessageResponse.from(result));
    }

    // 메시지/답글 수정 (같은 chat_message_mirror 레코드라 엔드포인트도 공용)
    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<Void> updateMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String messageId,
            @RequestBody UpdateMessageRequest request
    ) {
        chatMessageCommandUseCase.updateMessage(
                new UpdateMessageCommand(request.channelId(), messageId, principal.getId(), request.content())
        );
        return ResponseEntity.noContent().build();
    }

    // 메시지/답글 삭제
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String messageId,
            @RequestParam String channelId
    ) {
        chatMessageCommandUseCase.deleteMessage(
                new DeleteMessageCommand(channelId, messageId, principal.getId())
        );
        return ResponseEntity.noContent().build();
    }

    // 그룹 채팅방 상세 조회
    @GetMapping("/channels/{channelId}")
    public ResponseEntity<ChatChannelDetailResponse> getChannelDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String channelId
    ) {
        ChatChannelDetailResult result = chatChannelQueryUseCase.getChannelDetail(channelId, principal.getId());
        return ResponseEntity.ok(ChatChannelDetailResponse.from(result));
    }

    // 채팅 상대 검색 (새채팅 모달 전용)
    @GetMapping("/users")
    public ResponseEntity<List<SendbirdUserResponse>> searchUsers(@RequestParam String search) {
        List<SendbirdUserResponse> result = chatUserQueryUseCase.searchUsers(search).stream()
                .map(SendbirdUserResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // 채널 메시지 이력 조회
    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChannelMessages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String channelId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        List<ChatMessageResponse> result = chatMessageMirrorQueryUseCase
                .getChannelMessages(channelId, principal.getId(), pageable)
                .map(ChatMessageResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // 스레드 답글 조회
    @GetMapping("/messages/{messageId}/replies")
    public ResponseEntity<List<ChatMessageResponse>> getThreadReplies(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long messageId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        List<ChatMessageResponse> result = chatMessageMirrorQueryUseCase
                .getThreadReplies(messageId, principal.getId(), pageable)
                .map(ChatMessageResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // 메시지 통합 검색
    @GetMapping("/search")
    public ResponseEntity<Page<ChatMessageResponse>> searchMessages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ChatMessageSearchCondition condition =
                new ChatMessageSearchCondition(channelId, senderId, keyword, startDate, endDate);

        Page<ChatMessageResponse> result = chatMessageMirrorQueryUseCase
                .searchMessages(condition, principal.getId(), pageable)
                .map(ChatMessageResponse::from);

        return ResponseEntity.ok(result);
    }

    // 팀변경 채널 자동반영 (시스템 호출)
    @PatchMapping("/channels/team-change")
    public ResponseEntity<Void> updateChannelMembers(@RequestBody UpdateChannelMembersRequest request) {
        chatChannelCommandUseCase.updateChannelMembers(
                new UpdateChannelMembersCommand(request.channelId(), request.addUserIds(), request.removeUserIds())
        );
        return ResponseEntity.noContent().build();
    }

    // 팀 채팅방 자동 생성 (시스템 호출)
    @PostMapping("/teams/{teamId}/channel")
    public ResponseEntity<ChannelResponse> createTeamChannel(
            @PathVariable Long teamId,
            @RequestBody CreateTeamChannelRequest request
    ) {
        ChatChannelResult result = chatChannelCommandUseCase.createTeamChannel(teamId, request.memberUserIds());
        return ResponseEntity.ok(ChannelResponse.from(result));
    }

    // 온라인 상태 조회
    @GetMapping("/users/{userId}/status")
    public ResponseEntity<OnlineStatusResponse> getUserStatus(@PathVariable Long userId) {
        SendbirdUserStatus status = chatUserQueryUseCase.getUserStatus(userId);
        return ResponseEntity.ok(OnlineStatusResponse.from(status));
    }

}
