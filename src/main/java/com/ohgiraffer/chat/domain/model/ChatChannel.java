package com.ohgiraffer.chat.domain.model;

/*
 * comment.
 *  채팅 채널 도메인 모델
 *  Sendbird가 채널의 실제 소스이고, 우리 DB는 team_id 검색/채널 타입 등 도메인 조회용 미러링만 함
 */

public class ChatChannel {

    public enum ChannelType { DM, GROUP }

    private final Long id;
    private final String sendbirdChannelUrl;
    private final ChannelType channelType;
    private final String name;
    private final Long teamId;

    private ChatChannel(Long id, String sendbirdChannelUrl, ChannelType channelType, String name, Long teamId) {
        this.id = id;
        this.sendbirdChannelUrl = sendbirdChannelUrl;
        this.channelType = channelType;
        this.name = name;
        this.teamId = teamId;
    }

    // 신규 채널 생성 (Sendbird 생성 성공 후 우리 DB 미러링)
    public static ChatChannel create(String sendbirdChannelUrl, ChannelType channelType, String name, Long teamId) {
        return new ChatChannel(null, sendbirdChannelUrl, channelType, name, teamId);
    }

    // DB 조회값으로 도메인 객체 복원 (JpaEntity.toDomain()에서 사용)
    public static ChatChannel reconstitute(Long id, String sendbirdChannelUrl, ChannelType channelType,
                                           String name, Long teamId) {
        return new ChatChannel(id, sendbirdChannelUrl, channelType, name, teamId);
    }

    public Long getId() { return id; }
    public String getSendbirdChannelUrl() { return sendbirdChannelUrl; }
    public ChannelType getChannelType() { return channelType; }
    public String getName() { return name; }
    public Long getTeamId() { return teamId; }

}
