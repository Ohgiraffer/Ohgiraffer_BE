package com.ohgiraffer.chatbot.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
 * comment.
 *  Sendbird Bot 콜백 payload 파싱
 *  - 필드 경로(channel_url, user.user_id, message)는 ChatWebhookService의 그룹채널 웹훅 구조를 참고한 추정치
 *    실제 "유저 발화" 콜백 수신 로그로 아직 검증 안 됨 - 현재까지 관측된 콜백은 전부 IGNORED_CATEGORIES 케이스뿐이었음
 *  - category=group_channel:bot_message_send 는 봇이 "자기 자신이 보낸 메시지"에 대해 받는 에코 이벤트.
 *    실제 유저 메시지는 ChatMessageCommandService -> ChatbotOrchestrator.handleDirectMessage() 경로로 이미 처리되고 있어서,
 *    이 웹훅으로 유저 메시지가 들어올 일은 없는 것으로 보임. 에코를 유저 메시지로 오인해 처리하면
 *    봇 응답 -> 에코 수신 -> 봇 재응답으로 이어지는 무한루프 위험이 있으므로 반드시 여기서 먼저 걸러야 함.
 */

@Slf4j
@Component
public class ChatbotWebhookPayloadParser {

    private static final Set<String> IGNORED_CATEGORIES = Set.of(
            "group_channel:bot_message_send" // 봇 자신의 발화 에코 - 처리 대상 아님
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<ParsedBotMessage> parse(String rawPayload) {
        Map<String, Object> raw;
        try {
            raw = objectMapper.readValue(rawPayload, Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "봇 콜백 페이로드 파싱 실패");
        }

        String category = (String) raw.get("category");
        if (IGNORED_CATEGORIES.contains(category)) {
            log.debug("[ChatbotWebhookParser] 무시 대상 콜백 category={}", category);
            return Optional.empty();
        }

        // 추정 구조: { channel: { channel_url }, sender: { user_id }, payload: { message: "..." } }
        // 아직 실제 유저 발화 콜백 샘플로 검증되지 않음
        Map<String, Object> channel = (Map<String, Object>) raw.get("channel");
        Map<String, Object> sender = (Map<String, Object>) raw.get("sender");
        Map<String, Object> messageObj = (Map<String, Object>) raw.get("message");
        String message = messageObj != null ? (String) messageObj.get("text") : null;

        if (channel == null || sender == null || message == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY,
                    "봇 콜백 필수 필드 누락 - payload 구조 재검증 필요 | category=" + category);
        }

        Object channelUrlRaw = channel.get("channel_url");
        if (!(channelUrlRaw instanceof String channelUrl) || channelUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY,
                    "봇 콜백 channel_url 누락 또는 형식 오류");
        }

        Object userIdRaw = sender.get("user_id");
        if (!(userIdRaw instanceof String userIdStr) || !userIdStr.matches("\\d+")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY,
                    "봇 콜백 user_id 누락 또는 숫자 형식 아님");
        }

        return Optional.of(new ParsedBotMessage(channelUrl, Long.parseLong(userIdStr), message));
    }

    public record ParsedBotMessage(String channelId, Long senderId, String message) {}

}