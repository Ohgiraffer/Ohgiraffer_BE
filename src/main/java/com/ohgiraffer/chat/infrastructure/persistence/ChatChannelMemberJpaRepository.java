package com.ohgiraffer.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA가 구현체를 자동으로 생성
 *  -> Domain을 모르고 JpaEntity만 다룸
 */

public interface ChatChannelMemberJpaRepository extends JpaRepository<ChatChannelMemberJpaEntity, Long> {

    // 현재 참여 중인 멤버만 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelMemberJpaEntity> findAllByChatChannelIdAndLeftAtIsNull(Long chatChannelId);

    // 특정 채널의 특정 유저 멤버십 단건 조회 - Spring Data 이름 기반 자동구현
    Optional<ChatChannelMemberJpaEntity> findByChatChannelIdAndUserId(Long chatChannelId, Long userId);

    // 팀변경 제외 대상 일괄 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelMemberJpaEntity> findAllByChatChannelIdAndUserIdIn(Long chatChannelId, List<Long> userIds);

    // 유저가 현재 참여중인 채널 멤버십 전체 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelMemberJpaEntity> findAllByUserIdAndLeftAtIsNull(Long userId);

    // 여러 채널의 멤버 일괄 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelMemberJpaEntity> findAllByChatChannelIdInAndLeftAtIsNull(List<Long> chatChannelIds);

    // 유저 기준 채널별 안읽음수 - 부등호 조인 조건으로 DB에서 카운트까지 끝냄
    // COALESCE로 lastReadMessageId가 null(한번도 안읽음)인 경우 전체 카운트되도록 처리
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT v.chat_channel_id AS chatChannelId, COUNT(m.chat_message_id) AS unreadCount
            FROM (
                SELECT chat_channel_id, COALESCE(last_read_message_id, 0) AS last_read
                FROM chat_channel_member
                WHERE user_id = :userId AND left_at IS NULL
            ) v
            JOIN chat_channel c ON c.chat_channel_id = v.chat_channel_id
            LEFT JOIN chat_message_mirror m
                ON m.channel_id = c.sendbird_channel_url
                AND m.chat_message_id > v.last_read
                AND m.sender_id != :userId
                AND m.deleted_at IS NULL
            GROUP BY v.chat_channel_id
            """, nativeQuery = true)
    List<ChannelUnreadCountProjection> findUnreadCountsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    // 활성 멤버십 존재 여부 - Spring Data 이름 기반 자동구현
    boolean existsByChatChannelIdAndUserIdAndLeftAtIsNull(Long chatChannelId, Long userId);

}
