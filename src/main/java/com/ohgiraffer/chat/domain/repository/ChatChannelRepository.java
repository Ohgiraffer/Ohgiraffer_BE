package com.ohgiraffer.chat.domain.repository;

import com.ohgiraffer.chat.domain.model.ChatChannel;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  채팅 채널 저장소 인터페이스
 *  Sendbird 채널 URL을 키로 우리 DB와 매핑하는 계약을 정의함
 */

public interface ChatChannelRepository {

    // 신규 채널 저장 / 기존 채널 갱신(id 존재 시) 공용
    ChatChannel save(ChatChannel channel);

    // PK 기준 단건 조회
    Optional<ChatChannel> findById(Long id);

    // 웹훅/멤버갱신 시 채널 식별용 - Sendbird 채널 URL로 우리 DB 레코드 찾음
    Optional<ChatChannel> findBySendbirdChannelUrl(String sendbirdChannelUrl);

    // 팀변경 시 대상 채널 조회 - team_id로 해당 팀의 채널(들) 찾음
    List<ChatChannel> findAllByTeamId(Long teamId);

    // 멤버십 조회로 얻은 channelId 목록을 IN절로 일괄 조회 - 채널목록 N+1 방지용
    List<ChatChannel> findAllByIdIn(List<Long> ids);

    // 여러 채널을 Sendbird URL로 한 번에 조회 - 통합검색 결과에 섞인 여러 채널을 벌크 조회할 때 사용 (N+1 방지)
    List<ChatChannel> findAllBySendbirdChannelUrlIn(List<String> sendbirdChannelUrls);

    // Sendbird 채널 URL 기준으로 채널 멤버 미러링 데이터 삭제
    void deleteMembersBySendbirdChannelUrl(String sendbirdChannelUrl);

    // Sendbird 채널 URL 기준으로 채널 미러링 데이터 삭제
    void deleteBySendbirdChannelUrl(String sendbirdChannelUrl);
}