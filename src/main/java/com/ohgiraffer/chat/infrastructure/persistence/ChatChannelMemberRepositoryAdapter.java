package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * comment.
 *  domain/repository의 ChatChannelMemberRepository 구현체
 *  Domain <-> JpaEntity 변환을 이 클래스가 전담함
 */

@Repository
public class ChatChannelMemberRepositoryAdapter implements ChatChannelMemberRepository {

    private final ChatChannelMemberJpaRepository jpaRepository;

    public ChatChannelMemberRepositoryAdapter(ChatChannelMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // 신규 참여자 저장 - Domain -> JpaEntity 변환 후 저장, 결과를 다시 Domain으로 변환해 반환
    @Override
    public ChatChannelMember save(ChatChannelMember member) {
        return jpaRepository.save(ChatChannelMemberJpaEntity.from(member)).toDomain();
    }

    // 신규 참여자 일괄 저장
    @Override
    public List<ChatChannelMember> saveAll(List<ChatChannelMember> members) {
        List<ChatChannelMemberJpaEntity> entities = members.stream().map(ChatChannelMemberJpaEntity::from).toList();
        return jpaRepository.saveAll(entities).stream().map(ChatChannelMemberJpaEntity::toDomain).toList();
    }

    // 현재 참여 중인 멤버만 조회
    @Override
    public List<ChatChannelMember> findAllByChatChannelIdAndLeftAtIsNull(Long chatChannelId) {
        return jpaRepository.findAllByChatChannelIdAndLeftAtIsNull(chatChannelId)
                .stream().map(ChatChannelMemberJpaEntity::toDomain).toList();
    }

    // 특정 채널의 특정 유저 멤버십 단건 조회
    @Override
    public Optional<ChatChannelMember> findByChatChannelIdAndUserId(Long chatChannelId, Long userId) {
        return jpaRepository.findByChatChannelIdAndUserId(chatChannelId, userId).map(ChatChannelMemberJpaEntity::toDomain);
    }

    // 팀변경 제외 대상 일괄 조회
    @Override
    public List<ChatChannelMember> findAllByChatChannelIdAndUserIdIn(Long chatChannelId, List<Long> userIds) {
        return jpaRepository.findAllByChatChannelIdAndUserIdIn(chatChannelId, userIds)
                .stream().map(ChatChannelMemberJpaEntity::toDomain).toList();
    }

    // 유저 기준 참여 채널 멤버십 조회 - 채널목록(getChannelList)에서 channelId 목록 뽑는 용도
    @Override
    public Map<Long, Long> findUnreadCountsByUserId(Long userId) {
        return jpaRepository.findUnreadCountsByUserId(userId).stream()
                .collect(Collectors.toMap(
                        ChannelUnreadCountProjection::getChatChannelId,
                        ChannelUnreadCountProjection::getUnreadCount
                ));
    }

    // 여러 채널의 멤버 일괄 조회
    @Override
    public List<ChatChannelMember> findAllByChatChannelIdInAndLeftAtIsNull(List<Long> chatChannelIds) {
        return jpaRepository.findAllByChatChannelIdInAndLeftAtIsNull(chatChannelIds)
                .stream().map(ChatChannelMemberJpaEntity::toDomain).toList();
    }

    // 유저 기준 채널별 안읽음수 일괄 조회 - 네이티브쿼리 결과(Projection)를 Map으로 변환
    @Override
    public List<ChatChannelMember> findAllByUserIdAndLeftAtIsNull(Long userId) {
        return jpaRepository.findAllByUserIdAndLeftAtIsNull(userId)
                .stream().map(ChatChannelMemberJpaEntity::toDomain).toList();
    }

    // IDOR 방지 - 채널 데이터 노출 전 호출자가 실제 활성 멤버인지 확인
    @Override
    public boolean existsActiveMembership(Long chatChannelId, Long userId) {
        return jpaRepository.existsByChatChannelIdAndUserIdAndLeftAtIsNull(chatChannelId, userId);
    }

}
