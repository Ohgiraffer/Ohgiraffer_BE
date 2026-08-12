package com.ohgiraffer.chat.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageDeletedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageUpdatedCommand;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorCommandUseCase;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/*
 * comment.
 *  Sendbird 웹훅 페이로드 파싱 + 이벤트 종류별 라우팅
 *  category 필드로 이벤트 종류 판단 -> ChatMessageMirrorCommandUseCase에 위임
 *  주의: 아래 필드 경로(payload/channel/user/file 등)는 Sendbird 일반 웹훅 포맷 기준 추정치임.
 *        실제 수신 JSON으로 검증 필요함.
 */


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWebhookService {

    private final ChatMessageMirrorCommandUseCase chatMessageMirrorCommandUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 웹훅 진입점 - payload를 Map으로 파싱 후 category 값으로 이벤트 종류 분기
    @SuppressWarnings("unchecked")
    public void handle(String payload) {
        Map<String, Object> raw;
        try {
            raw = objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("[Chat] 웹훅 페이로드 파싱 실패 | payload={}", payload, e);
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "웹훅 페이로드 파싱 실패");
        }

        String category = (String) raw.get("category");

        switch (category) {
            case "group_channel:message_send" -> mirrorCreated(raw);
            case "group_channel:message_update" -> mirrorUpdated(raw);
            case "group_channel:message_delete" -> mirrorDeleted(raw);
            default -> log.info("[Chat] 미처리 웹훅 이벤트 - 스킵 | category={}", category);
        }
    }

    // 메시지/답글 전송 이벤트 파싱 - parent_message_id 있으면 답글로 판단, mirrorCreated로 위임
    @SuppressWarnings("unchecked")
    private void mirrorCreated(Map<String, Object> raw) {
        Map<String, Object> payloadMsg = (Map<String, Object>) raw.get("payload");
        Map<String, Object> channel = (Map<String, Object>) raw.get("channel");
        Map<String, Object> sender = (Map<String, Object>) payloadMsg.get("user");
        Map<String, Object> file = (Map<String, Object>) payloadMsg.get("file");
        Object parentMessageIdRaw = payloadMsg.get("parent_message_id");

        MirrorMessageCreatedCommand command = new MirrorMessageCreatedCommand(
                (String) channel.get("channel_url"),
                String.valueOf(payloadMsg.get("message_id")),
                parentMessageIdRaw != null ? ((Number) parentMessageIdRaw).longValue() : null,
                sender != null ? Long.parseLong((String) sender.get("user_id")) : null,
                (String) payloadMsg.get("message"),
                file != null ? (String) file.get("url") : null,
                file != null ? (String) file.get("type") : null,
                Instant.ofEpochMilli(((Number) payloadMsg.get("created_at")).longValue())
        );

        chatMessageMirrorCommandUseCase.mirrorCreated(command);
    }

    // 메시지/답글 수정 이벤트 파싱 - mirrorUpdated로 위임
    @SuppressWarnings("unchecked")
    private void mirrorUpdated(Map<String, Object> raw) {
        Map<String, Object> payloadMsg = (Map<String, Object>) raw.get("payload");
        Map<String, Object> file = (Map<String, Object>) payloadMsg.get("file");
        Object updatedAtRaw = payloadMsg.get("updated_at");

        if (updatedAtRaw == null) {
            log.warn("[Chat] 수정 이벤트에 updated_at 필드 없음 - 순서 검증 불가로 스킵 | messageId={}",
                    payloadMsg.get("message_id"));
            return;
        }

        MirrorMessageUpdatedCommand command = new MirrorMessageUpdatedCommand(
                String.valueOf(payloadMsg.get("message_id")),
                (String) payloadMsg.get("message"),
                file != null ? (String) file.get("url") : null,
                Instant.ofEpochMilli(((Number) updatedAtRaw).longValue())
        );

        chatMessageMirrorCommandUseCase.mirrorUpdated(command);
    }

    // 메시지/답글 삭제 이벤트 파싱 - mirrorDeleted로 위임
    @SuppressWarnings("unchecked")
    private void mirrorDeleted(Map<String, Object> raw) {
        Map<String, Object> payloadMsg = (Map<String, Object>) raw.get("payload");
        Object deletedAtRaw = payloadMsg.get("deleted_at");

        if (deletedAtRaw == null) {
            log.warn("[Chat] 삭제 이벤트에 deleted_at 필드 없음 - 순서 검증 불가로 스킵 | messageId={}",
                    payloadMsg.get("message_id"));
            return;
        }

        MirrorMessageDeletedCommand command = new MirrorMessageDeletedCommand(
                String.valueOf(payloadMsg.get("message_id")),
                Instant.ofEpochMilli(((Number) deletedAtRaw).longValue())
        );

        chatMessageMirrorCommandUseCase.mirrorDeleted(command);
    }

}
