package com.ohgiraffer.chat.application.result;

import java.time.LocalDateTime;

/*
 * comment.
 *  Sendbird 메시지 전송/답글 결과
 *  전송 직후 우리 DB(chat_message_mirror)에 미러링할 때 필요한 최소 필드만 담음
 *  -> 웹훅으로도 같은 이벤트가 들어오지만, 전송 응답 시점에 바로 미러링해서 지연 없이 반영하기 위함
 */

public record SendbirdMessageResult(
        String sendbirdMessageId,
        String channelId,
        Long senderId,
        String content,
        LocalDateTime sentAt
) {
}
