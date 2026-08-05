package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * comment.
 *  DB 테이블(chat_channel)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model(ChatChannel)을 모르고 DB 컬럼 구조만 표현
 *  -> created_at/updated_at 없음 (DDL 원본에 시간 컬럼 없어 BaseTimeEntity 미상속)
 */

@Getter
@Entity
@Table(name = "chat_channel")
@NoArgsConstructor
public class ChatChannelJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_channel_id")
    private Long id;

    @Column(name = "sendbird_channel_url", nullable = false, length = 255)
    private String sendbirdChannelUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private ChatChannel.ChannelType channelType;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "team_id")
    private Long teamId;

    // Domain -> JpaEntity 변환 (저장용)
    public static ChatChannelJpaEntity from(ChatChannel domain) {
        ChatChannelJpaEntity entity = new ChatChannelJpaEntity();
        entity.id = domain.getId();
        entity.sendbirdChannelUrl = domain.getSendbirdChannelUrl();
        entity.channelType = domain.getChannelType();
        entity.name = domain.getName();
        entity.teamId = domain.getTeamId();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public ChatChannel toDomain() {
        return ChatChannel.reconstitute(id, sendbirdChannelUrl, channelType, name, teamId);
    }

}
