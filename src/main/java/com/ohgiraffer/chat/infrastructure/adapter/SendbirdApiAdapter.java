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
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/*
 * comment.
 *  SendbirdApiPort 구현체 - Sendbird Platform API를 RestClient로 직접 호출함
 *  RestClient(sendbirdRestClient)는 base-url/인증헤더가 설정 클래스에서 미리 구성되어 주입됨
 *  4xx는 대부분 그대로 CHAT_SENDBIRD_API_ERROR로 전파, 유저 중복 생성(400202)만 별도 분기 처리
 */

@Component
public class SendbirdApiAdapter implements SendbirdApiPort {

    private final RestClient restClient;
    private final SendbirdProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Bean 주입 제거, 직접 생성

    public SendbirdApiAdapter(RestClient sendbirdRestClient, SendbirdProperties properties) {
        this.restClient = sendbirdRestClient;
        this.properties = properties;
    }

    // 로그인 성공 시 Sendbird 유저 생성 또는 재사용 - 이미 존재하면 토큰만 재발급
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

    // 이미 존재하는 유저의 access token만 재발급받음 - 유저 신규 생성 없이 토큰만 갱신
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

    // Sendbird 유저 생성 응답(raw Map)을 SendbirdUserProvisionResult로 변환
    private SendbirdUserProvisionResult toProvisionResult(Map<String, Object> raw) {
        return new SendbirdUserProvisionResult(
                Long.parseLong((String) raw.get("user_id")),
                (String) raw.get("nickname"),
                (String) raw.get("access_token")
        );
    }

    // 닉네임 접두어(startswith) 기준 채팅 상대 검색
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

    // 채팅방 생성 - userIds 1명이면 1:1(is_distinct=true), 2명 이상이면 그룹
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

    // 팀 채팅방 자동 생성 - 이름 규칙("team-{teamId}")만 다르고 나머지는 일반 채널 생성과 동일해서 재사용
    @Override
    public String createTeamChannel(Long teamId, List<Long> memberUserIds) {
        // 팀 채팅방은 이름 규칙만 다르고 나머지는 일반 채널 생성과 동일
        return createChannel(memberUserIds, "team-" + teamId);
    }

    // 팀변경 시 채널 멤버 초대/제외 반영 - 초대(invite)와 제외(leave)를 각각 별도 API 호출로 처리
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

    // 메시지 전송 - 첨부파일 유무로 FILE/MESG 타입 분기, 멘션 있으면 mentioned_user_ids 추가
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

    // 메시지 수정 - 본문 텍스트만 갱신
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

    // 메시지 삭제
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

    // 스레드 답글 작성 - parent_message_id로 원본 메시지 참조, 첨부파일 유무로 FILE/MESG 분기(sendMessage와 동일 로직)
    @Override
    public SendbirdMessageResult sendReply(String channelId, Long parentMessageId, Long senderId,
                                           String content, String attachmentUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(senderId));
        body.put("parent_message_id", parentMessageId);

        // sendMessage와 동일하게 첨부파일 유무로 message_type 분기
        if (attachmentUrl != null) {
            body.put("message_type", "FILE");
            body.put("file", Map.of("url", attachmentUrl));
            body.put("message", content == null ? "" : content);
        } else {
            body.put("message_type", "MESG");
            body.put("message", content);
        }

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

    // 온라인 상태 조회 - 오프라인이면 마지막 접속시각(last_seen_at, epoch millis)까지 변환해서 반환
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

    // 웹훅 서명 검증 - HMAC-SHA256으로 payload를 해싱해서 헤더의 signature와 상수시간 비교
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

    // Sendbird 유저 검색 응답(raw Map)을 SendbirdUserResult로 변환
    private SendbirdUserResult toUserResult(Map<String, Object> raw) {
        return new SendbirdUserResult(
                Long.parseLong((String) raw.get("user_id")),
                (String) raw.get("nickname"),
                (String) raw.get("profile_url"),
                Boolean.TRUE.equals(raw.get("is_online"))
        );
    }

    // Sendbird 메시지 전송/답글 응답(raw Map)을 SendbirdMessageResult로 변환 - created_at(epoch millis)을 Instant로 변환
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
