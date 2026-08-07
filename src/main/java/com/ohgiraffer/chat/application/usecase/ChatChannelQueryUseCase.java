package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.result.ChatChannelDetailResult;
import com.ohgiraffer.chat.application.result.ChatChannelListItemResult;
import com.ohgiraffer.chat.domain.model.ChatChannel;

import java.util.List;

/*
 * comment.
 *  채팅방 상세 조회 계약
 */

public interface ChatChannelQueryUseCase {

    // 그룹 채팅방 상세 조회 - 참여자 목록 + 읽음 인원 포함
    ChatChannelDetailResult getChannelDetail(String channelId, Long principalId);

    // 참여 채팅방 목록 조회 - type이 null이면 전체, DM/GROUP이면 필터, search가 있으면 채널 이름 부분검색 추가 필터
    List<ChatChannelListItemResult> getChannelList(Long userId, ChatChannel.ChannelType type, String search);

    // 헤더에 상시 노출되는 안읽은 채팅 총 개수 (채널 목록 전체를 안 불러오고 합계만 가볍게 조회)
    long getTotalUnreadCount(Long userId);

}
