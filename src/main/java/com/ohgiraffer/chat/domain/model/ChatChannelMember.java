package com.ohgiraffer.chat.domain.model;

import java.time.Instant;

/*
 * comment.
 *  채팅 채널 참여자 도메인 모델
 *  안읽음 수 계산(참여 채팅방 목록 조회)에 lastReadMessageId/lastReadAt 사용
 *  leftAt은 소프트 탈퇴 표시 - 탈퇴해도 이력 보존을 위해 하드 삭제하지 않음
 */

public class ChatChannelMember {

    private final Long id;
    private final Long chatChannelId;
    private final Long userId;
    private final Instant joinedAt;
    private Instant leftAt;
    private Long lastReadMessageId;
    private Instant lastReadAt;

    private ChatChannelMember(Long id, Long chatChannelId, Long userId, Instant joinedAt,
                              Instant leftAt, Long lastReadMessageId, Instant lastReadAt) {
        this.id = id;
        this.chatChannelId = chatChannelId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = lastReadAt;
    }

    // 채널 참여 (신규 초대/생성 시점, joinedAt은 현재 시각으로 즉시 확정)
    public static ChatChannelMember join(Long chatChannelId, Long userId) {
        return new ChatChannelMember(null, chatChannelId, userId, Instant.now(), null, null, null);
    }

    // DB 조회값으로 도메인 객체 복원
    public static ChatChannelMember reconstitute(Long id, Long chatChannelId, Long userId, Instant joinedAt,
                                                 Instant leftAt, Long lastReadMessageId, Instant lastReadAt) {
        return new ChatChannelMember(id, chatChannelId, userId, joinedAt, leftAt, lastReadMessageId, lastReadAt);
    }

    // 채널 탈퇴 (팀변경으로 제외될 때) - 이미 탈퇴했으면 재처리 안 함(멱등)
    public void leave() {
        if (this.leftAt == null) {
            this.leftAt = Instant.now();
        }
    }

    // 탈퇴했던 멤버가 재입장 - leftAt을 초기화하고 joinedAt은 그대로 이력 유지 (최초 입장일이 아니라 최근 재입장이 필요하면 별도 필드 고려)
    public void rejoin() {
        this.leftAt = null;
    }

    // 메시지 읽음 처리 - 마지막으로 읽은 메시지 id/시각 갱신 (안읽음 수 계산 기준점)
    public void markRead(Long messageId) {
        this.lastReadMessageId = messageId;
        this.lastReadAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getChatChannelId() { return chatChannelId; }
    public Long getUserId() { return userId; }
    public Instant getJoinedAt() { return joinedAt; }
    public Instant getLeftAt() { return leftAt; }
    public Long getLastReadMessageId() { return lastReadMessageId; }
    public Instant getLastReadAt() { return lastReadAt; }

}
