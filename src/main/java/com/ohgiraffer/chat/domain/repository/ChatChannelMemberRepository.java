package com.ohgiraffer.chat.domain.repository;

import com.ohgiraffer.chat.domain.model.ChatChannelMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * comment.
 *  채팅 채널 참여자 저장소 인터페이스
 */

public interface ChatChannelMemberRepository {

    // 신규 참여자 저장
    ChatChannelMember save(ChatChannelMember member);

    // 신규 참여자 일괄 저장 - 채널 생성 시 초대 인원 전체 등록용
    List<ChatChannelMember> saveAll(List<ChatChannelMember> members);

    // 현재 참여 중인 멤버만 - 탈퇴(leftAt != null)한 멤버는 제외
    List<ChatChannelMember> findAllByChatChannelIdAndLeftAtIsNull(Long chatChannelId);

    // 특정 채널의 특정 유저 멤버십 단건 조회 - 본인 여부 확인용
    Optional<ChatChannelMember> findByChatChannelIdAndUserId(Long chatChannelId, Long userId);

    // 팀변경 제외 대상 일괄 조회용 - removeUserIds에 해당하는 멤버십만 뽑아서 leave() 처리할 시 사용
    List<ChatChannelMember> findAllByChatChannelIdAndUserIdIn(Long chatChannelId, List<Long> userIds);

    // 유저 기준 채널별 안읽음수 일괄 조회 - key는 chat_channel_id(PK), DB에서 조인+집계까지 끝내고 옴
    Map<Long, Long> findUnreadCountsByUserId(Long userId);

    // 유저가 현재 참여중인 채널 멤버십 전체 조회 - 채널목록에서 channelId 목록 뽑는 용도
    List<ChatChannelMember> findAllByUserIdAndLeftAtIsNull(Long userId);

    // 활성 멤버십 여부만 빠르게 확인 (IDOR 방지용) - 상세/이력/답글/검색 조회 전 필수 체크
    boolean existsActiveMembership(Long chatChannelId, Long userId);
}
