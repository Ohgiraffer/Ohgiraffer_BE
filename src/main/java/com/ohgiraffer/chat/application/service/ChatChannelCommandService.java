package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.CreateChannelCommand;
import com.ohgiraffer.chat.application.command.UpdateChannelMembersCommand;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.ChatChannelResult;
import com.ohgiraffer.chat.application.usecase.ChatChannelCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  ChatChannelCommandUseCase 구현체
 *  Sendbird에 먼저 채널/멤버 반영 성공한 다음, 그 결과(sendbirdChannelUrl)를 우리 DB에도 미러링 저장함
 *  - team_id 검색, 채널 타입 조회 같은 도메인 쿼리를 Sendbird API 호출 없이 우리 DB에서 바로 처리하기 위함
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatChannelCommandService implements ChatChannelCommandUseCase {

    private final SendbirdApiPort sendbirdApiPort;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    // 채팅방 생성 - Sendbird 반영 성공 후 채널/참여자 전원을 우리 DB에 미러링
    @Override
    @Transactional
    public ChatChannelResult createChannel(CreateChannelCommand command) {
        String sendbirdChannelUrl = sendbirdApiPort.createChannel(command.userIds(), command.name());

        // 1명이면 1:1(DM), 2명 이상이면 그룹 - SendbirdApiAdapter의 is_distinct 판단 기준과 동일하게 맞춤
        ChatChannel.ChannelType type = command.userIds().size() <= 1
                ? ChatChannel.ChannelType.DM
                : ChatChannel.ChannelType.GROUP;

        ChatChannel savedChannel = chatChannelRepository.save(
                ChatChannel.create(sendbirdChannelUrl, type, command.name(), null)
        );

        // 채널 생성 시 지정된 유저 전원을 참여자로 즉시 등록
        List<ChatChannelMember> members = command.userIds().stream()
                .map(userId -> ChatChannelMember.join(savedChannel.getId(), userId))
                .toList();
        chatChannelMemberRepository.saveAll(members);

        log.info("[Chat] 채널 생성 완료 | channelId={}, memberCount={}",
                sendbirdChannelUrl, command.userIds().size());

        return new ChatChannelResult(sendbirdChannelUrl, command.name());
    }

    // 팀변경 채널 자동반영 - Sendbird에 먼저 초대/제외 반영, 실패하면 우리 DB는 안 건드림
    @Override
    @Transactional
    public void updateChannelMembers(UpdateChannelMembersCommand command) {
        // Sendbird에 먼저 반영 - 실패하면 우리 DB도 건드리지 않음 (Sendbird가 source of truth)
        sendbirdApiPort.updateChannelMembers(command.channelId(), command.addUserIds(), command.removeUserIds());

        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(command.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        if (command.addUserIds() != null && !command.addUserIds().isEmpty()) {
            for (Long userId : command.addUserIds()) {
                // 이미 탈퇴 이력이 있는 멤버면 새 row 대신 기존 row를 재활성화 (leftAt=null로 리셋)
                Optional<ChatChannelMember> existing = chatChannelMemberRepository
                        .findByChatChannelIdAndUserId(channel.getId(), userId);

                if (existing.isPresent()) {
                    existing.get().rejoin(); // 아래 도메인 모델에 메서드 추가 필요
                    chatChannelMemberRepository.save(existing.get());
                } else {
                    chatChannelMemberRepository.save(ChatChannelMember.join(channel.getId(), userId));
                }
            }
        }

        if (command.removeUserIds() != null && !command.removeUserIds().isEmpty()) {
            // 하드 삭제 대신 leave() 처리 - 채팅 이력 조회 시 과거 참여자 정보가 남아있어야 함
            List<ChatChannelMember> toRemove = chatChannelMemberRepository
                    .findAllByChatChannelIdAndUserIdIn(channel.getId(), command.removeUserIds());
            toRemove.forEach(ChatChannelMember::leave);
            chatChannelMemberRepository.saveAll(toRemove);
        }

        log.info("[Chat] 채널 멤버 갱신 완료 | channelId={}", command.channelId());
    }

    // 팀 채팅방 자동 생성 - team_id를 채워서 저장, 팀변경 시 findAllByTeamId로 대상 채널 조회 가능하게 함
    @Override
    @Transactional
    public ChatChannelResult createTeamChannel(Long teamId, List<Long> memberUserIds) {
        String sendbirdChannelUrl = sendbirdApiPort.createTeamChannel(teamId, memberUserIds);

        // 팀 채널은 항상 GROUP, team_id를 채워서 CHAT-011 팀변경 시 조회 가능하게 함
        ChatChannel savedChannel = chatChannelRepository.save(
                ChatChannel.create(sendbirdChannelUrl, ChatChannel.ChannelType.GROUP, "team-" + teamId, teamId)
        );

        List<ChatChannelMember> members = memberUserIds.stream()
                .map(userId -> ChatChannelMember.join(savedChannel.getId(), userId))
                .toList();
        chatChannelMemberRepository.saveAll(members);

        log.info("[Chat] 팀 채널 자동 생성 완료 | teamId={}, channelId={}", teamId, sendbirdChannelUrl);

        return new ChatChannelResult(sendbirdChannelUrl, "team-" + teamId);
    }

}
