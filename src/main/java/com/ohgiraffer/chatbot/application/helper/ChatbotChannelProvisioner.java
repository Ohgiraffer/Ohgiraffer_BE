package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chatbot.application.port.ChatbotChannelPort;
import com.ohgiraffer.chatbot.infrastructure.config.ChatbotBotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  AI비서 채널 신규 생성 로직
 *  - Sendbird 채널 생성 -> 봇 초대 -> 로컬 DB(ChatChannel/ChatChannelMember) 미러링 -> Redis 매핑 저장 순서
 *  - ChatChannelCommandService.createChannel()과 동일하게 로컬 DB 미러링을 반드시 같이 해야
 *    기존 /chat/channels/{channelId}/messages 등 API가 이 채널을 인식할 수 있음
 *  - 봇은 문자열 ID라 users FK 제약이 있는 ChatChannelMember에는 등록하지 않음
 *    (봇 메시지는 sendBotMessage()로 별도 경로 발신이라 멤버십 검증을 타지 않음)
 *  - @Transactional은 DB(3,4단계)만 커버함. Sendbird 채널 생성(1)/봇 초대(2)는 DB 트랜잭션 밖의 외부 API 호출이라
 *    이후 단계가 실패해도 자동으로 롤백되지 않음 -> 고아 Sendbird 채널이 남는 걸 방지하기 위해 수동 보상 처리함
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotChannelProvisioner {

    private final SendbirdApiPort sendbirdApiPort;
    private final ChatbotChannelPort chatbotChannelPort;
    private final ChatbotBotProperties chatbotBotProperties;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    @Transactional
    public String createAndSave(Long userId) {
        log.info("[ChatbotChannel] 신규 채널 생성 시작 | userId={}", userId);

        // 1. Sendbird에 1:1 채널 생성 (유저 본인만 지정 - 봇은 별도 초대)
        String channelUrl = sendbirdApiPort.createChannel(List.of(userId), "AI비서");

        try {
            // 2. 봇을 Sendbird 채널에 초대 (로컬 DB에는 반영 안 함 - 봇은 users 테이블 대상 아님)
            sendbirdApiPort.inviteBotToChannel(channelUrl, chatbotBotProperties.getUserId());

            // 3. 로컬 DB 미러링 - ChatChannelCommandService.createChannel()과 동일 패턴
            //    이게 빠지면 기존 /chat/channels/{channelId}/... API들이 이 채널을 "존재하지 않음"으로 판단함
            ChatChannel savedChannel = chatChannelRepository.save(
                    ChatChannel.create(channelUrl, ChatChannel.ChannelType.DM, "AI비서", null)
            );
            chatChannelMemberRepository.save(ChatChannelMember.join(savedChannel.getId(), userId));

            // 4. Redis에 유저-채널 매핑 저장 (조회 캐시용)
            chatbotChannelPort.saveChannelUrl(userId, channelUrl);

            log.info("[ChatbotChannel] 신규 채널 생성 완료 | userId={}, channelUrl={}", userId, channelUrl);
            return channelUrl;

        } catch (RuntimeException e) {
            // 채널 생성(1) 이후 단계가 실패한 경우 - Sendbird에 고아 채널이 남지 않도록 정리 시도
            log.error("[ChatbotChannel] 채널 생성 중 오류 발생 - Sendbird 채널 정리 시도 | userId={}, channelUrl={}", userId, channelUrl, e);
            cleanupOrphanedChannel(channelUrl);
            throw e; // 원래 실패 원인은 그대로 호출부로 전파 (마스킹하지 않음)
        }
    }

    // Sendbird 채널 삭제 시도 - 정리 자체가 실패해도 원래 예외를 덮지 않고 로그만 남김 (수동 확인 대상)
    private void cleanupOrphanedChannel(String channelUrl) {
        try {
            sendbirdApiPort.deleteChannel(channelUrl);
            log.info("[ChatbotChannel] 고아 Sendbird 채널 정리 완료 | channelUrl={}", channelUrl);
        } catch (RuntimeException cleanupException) {
            log.error("[ChatbotChannel] 고아 Sendbird 채널 정리 실패 - 수동 확인 필요 | channelUrl={}", channelUrl, cleanupException);
        }
    }

}
