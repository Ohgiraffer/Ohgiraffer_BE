package com.ohgiraffer.chat.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;
import com.ohgiraffer.chat.application.result.SendbirdUserProvisionResult;
import com.ohgiraffer.chat.application.result.SendbirdUserResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Component
public class SendbirdApiAdapter implements SendbirdApiPort {

    private final RestClient restClient;
    private final SendbirdProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Bean 주입 제거, 직접 생성

    public SendbirdApiAdapter(RestClient sendbirdRestClient, SendbirdProperties properties) {
        this.restClient = sendbirdRestClient;
        this.properties = properties;
    }

    @Override
    public SendbirdUserProvisionResult provisionUser(Long userId, String nickname, String profileUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(userId));
        body.put("nickname", nickname);
        body.put("profile_url", profileUrl == null ? "" : profileUrl);
        body.put("issue_access_token", true); // 유저 생성과 동시에 access token 발급

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/users")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return toProvisionResult(response);

        } catch (HttpClientErrorException e) {
            if (isUserAlreadyExists(e)) {
                // 이미 등록된 유저 - 신규 생성 대신 토큰만 재발급
                return reissueAccessToken(userId, nickname);
            }
            // 그 외 4xx는 진짜 오류이므로 그대로 전파
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR,
                    "Sendbird 유저 생성 실패 (status=" + e.getStatusCode() + ")");

        } catch (RestClientException e) {
            // 네트워크 오류, 5xx 등 - 재시도 유도가 필요한 진짜 시스템 오류
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 유저 생성 중 통신 오류 발생");
        }
    }

    // Sendbird 에러 코드 400202(user_id already exists)인지 응답 바디 파싱해서 확인
    private boolean isUserAlreadyExists(HttpClientErrorException e) {
        try {
            Map<String, Object> errorBody = objectMapper.readValue(
                    e.getResponseBodyAsString(), Map.class
            );
            Object code = errorBody.get("code");
            return code != null && code.toString().equals("400202");
        } catch (Exception parseException) {
            return false; // 파싱 자체가 안 되면 안전하게 "존재하지 않음"으로 처리 -> 진짜 오류로 흘려보냄
        }
    }

    private SendbirdUserProvisionResult reissueAccessToken(Long userId, String nickname) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/users/{user_id}/token", userId)
                    .body(Map.of())
                    .retrieve()
                    .body(Map.class);

            String accessToken = (String) response.get("token");
            return new SendbirdUserProvisionResult(userId, nickname, accessToken);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 유저 토큰 재발급 실패");
        }
    }

    private SendbirdUserProvisionResult toProvisionResult(Map<String, Object> raw) {
        return new SendbirdUserProvisionResult(
                Long.parseLong((String) raw.get("user_id")),
                (String) raw.get("nickname"),
                (String) raw.get("access_token")
        );
    }

    @Override
    public List<SendbirdUserResult> searchUsers(String query) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users")
                            .queryParam("nickname_startswith", query)
                            .build())
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> users = (List<Map<String, Object>>) response.get("users");

            return users.stream()
                    .map(this::toUserResult)
                    .toList();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 유저 검색 실패");
        }
    }

    @Override
    public String createChannel(List<Long> userIds, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_ids", userIds.stream().map(String::valueOf).toList());
        body.put("name", name == null ? "" : name);
        body.put("is_distinct", userIds.size() <= 1);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/group_channels")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("channel_url");
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 채널 생성 실패");
        }
    }

    @Override
    public String createTeamChannel(Long teamId, List<Long> memberUserIds) {
        // 팀 채팅방은 이름 규칙만 다르고 나머지는 일반 채널 생성과 동일
        return createChannel(memberUserIds, "team-" + teamId);
    }

    @Override
    public void updateChannelMembers(String channelId, List<Long> addUserIds, List<Long> removeUserIds) {
        try {
            if (addUserIds != null && !addUserIds.isEmpty()) {
                restClient.post()
                        .uri("/group_channels/{channel_url}/invite", channelId)
                        .body(Map.of("user_ids", addUserIds.stream().map(String::valueOf).toList()))
                        .retrieve()
                        .toBodilessEntity();
            }
            if (removeUserIds != null && !removeUserIds.isEmpty()) {
                restClient.put()
                        .uri("/group_channels/{channel_url}/leave", channelId)
                        .body(Map.of("user_ids", removeUserIds.stream().map(String::valueOf).toList()))
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 채널 멤버 갱신 실패");
        }
    }

    @Override
    public SendbirdMessageResult sendMessage(String channelId, Long senderId, String content,
                                             String attachmentUrl, List<Long> mentionedUserIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(senderId));

        if (attachmentUrl != null) {
            body.put("message_type", "FILE");
            body.put("file", Map.of("url", attachmentUrl));
            body.put("message", content == null ? "" : content);
        } else {
            body.put("message_type", "MESG");
            body.put("message", content);
        }

        if (mentionedUserIds != null && !mentionedUserIds.isEmpty()) {
            body.put("mentioned_user_ids", mentionedUserIds.stream().map(String::valueOf).toList());
            body.put("mention_type", "users");
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/group_channels/{channel_url}/messages", channelId)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return toMessageResult(response, channelId);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 메시지 전송 실패");
        }
    }

    @Override
    public void updateMessage(String channelId, String sendbirdMessageId, String newContent) {
        Map<String, Object> body = Map.of("message", newContent);

        try {
            restClient.put()
                    .uri("/group_channels/{channel_url}/messages/{message_id}", channelId, sendbirdMessageId)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 메시지 수정 실패");
        }
    }

    @Override
    public void deleteMessage(String channelId, String sendbirdMessageId) {
        try {
            restClient.delete()
                    .uri("/group_channels/{channel_url}/messages/{message_id}", channelId, sendbirdMessageId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 메시지 삭제 실패");
        }
    }

    @Override
    public SendbirdMessageResult sendReply(String channelId, Long parentMessageId, Long senderId, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("message_type", "MESG");
        body.put("user_id", String.valueOf(senderId));
        body.put("message", content);
        body.put("parent_message_id", parentMessageId);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/group_channels/{channel_url}/messages", channelId)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return toMessageResult(response, channelId);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 답글 전송 실패");
        }
    }

    @Override
    public SendbirdUserStatus getUserStatus(Long userId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/users/{user_id}", userId)
                    .retrieve()
                    .body(Map.class);

            boolean isOnline = Boolean.TRUE.equals(response.get("is_online"));
            Object lastSeenAtRaw = response.get("last_seen_at");
            Instant lastSeenAt = null;

            if (!isOnline && lastSeenAtRaw != null) {
                long epochMillis = ((Number) lastSeenAtRaw).longValue();
                if (epochMillis > 0) {
                    lastSeenAt = Instant.ofEpochMilli(epochMillis);
                }
            }

            return new SendbirdUserStatus(userId, isOnline, lastSeenAt);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.CHAT_SENDBIRD_API_ERROR, "Sendbird 온라인 상태 조회 실패");
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.apiToken().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CHAT_WEBHOOK_SIGNATURE_INVALID, "웹훅 서명 검증 중 오류 발생");
        }
    }

    private SendbirdUserResult toUserResult(Map<String, Object> raw) {
        return new SendbirdUserResult(
                Long.parseLong((String) raw.get("user_id")),
                (String) raw.get("nickname"),
                (String) raw.get("profile_url"),
                Boolean.TRUE.equals(raw.get("is_online"))
        );
    }

    private SendbirdMessageResult toMessageResult(Map<String, Object> raw, String channelId) {
        String senderIdRaw = raw.get("user") != null
                ? String.valueOf(((Map<String, Object>) raw.get("user")).get("user_id"))
                : null;

        long createdAtMillis = ((Number) raw.get("created_at")).longValue();
        Map<String, Object> file = (Map<String, Object>) raw.get("file");
        String attachmentUrl = file != null ? (String) file.get("url") : null;

        return new SendbirdMessageResult(
                String.valueOf(raw.get("message_id")),
                channelId,
                senderIdRaw != null ? Long.parseLong(senderIdRaw) : null,
                (String) raw.get("message"),
                attachmentUrl,
                (String) raw.get("type"),
                Instant.ofEpochMilli(createdAtMillis)
        );
    }

}
