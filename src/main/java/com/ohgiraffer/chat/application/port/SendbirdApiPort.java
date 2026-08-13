package com.ohgiraffer.chat.application.port;

import com.ohgiraffer.chat.application.result.*;

import java.util.List;

/*
 * comment.
 *  Sendbird Platform API 호출 계약
 */

public interface SendbirdApiPort {

    // 로그인 성공 시 샌드버드 유저 생성 또는 재사용 (채팅 유저 프로비저닝)
    SendbirdUserProvisionResult provisionUser(Long userId, String name, String profileUrl);

    // 채팅 상대 검색
    List<SendbirdUserResult> searchUsers(String query);

    // 채팅방 생성 - 1명이면 1:1, 2명 이상이면 그룹
    String createChannel(List<Long> userIds, String name);

    // 팀 채팅방 자동 생성
    String createTeamChannel(Long teamId, List<Long> memberUserIds);

    // 팀변경 시 채널 멤버 초대/제외 반영
    void updateChannelMembers(String channelId, List<Long> addUserIds, List<Long> removeUserIds);

    // 팀/그룹 채널 삭제
    void deleteChannel(String channelId);

    // 메시지 전송 - 텍스트/파일/멘션 포함
    SendbirdMessageResult sendMessage(String channelId, Long senderId, String content, String attachmentUrl, List<Long> mentionedUserIds);

    // 메시지 수정
    void updateMessage(String channelId, String sendbirdMessageId, String messageType, String newContent, String newAttachmentUrl);

    // 메시지 삭제
    void deleteMessage(String channelId, String sendbirdMessageId);

    // 스레드 답글 작성
    SendbirdMessageResult sendReply(String channelId, Long parentMessageId, Long senderId, String content, String attachmentUrl);

    // 온라인 상태 조회
    SendbirdUserStatus getUserStatus(Long userId);

    // 여러 유저 온라인 상태 일괄 조회 (DM 목록 온라인 표시용, N+1 방지)
    List<SendbirdUserStatus> getUserStatuses(List<Long> userIds);

    // 웹훅 서명 검증
    boolean verifyWebhookSignature(String payload, String signature);

    // 봇 리소스 신규 등록 - 최초 1회만 호출되는 관리성 작업 (프로퍼티에 bot_userid/token 저장해두고 재사용 권장)
    SendbirdBotProvisionResult registerBot(String botUserId, String nickname, String profileUrl, String botCallbackUrl, boolean isPrivacyMode);

    // 봇 전용 메시지 전송 API
    SendbirdMessageResult sendBotMessage(String botUserId, String channelId, String content);

    // 봇을 채널에 초대 - 봇 userId는 문자열이라 기존 updateChannelMembers(Long)와 별도 메서드 필요
    void inviteBotToChannel(String channelId, String botUserId);

}