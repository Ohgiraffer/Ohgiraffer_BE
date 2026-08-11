package com.ohgiraffer.chat.application.command;

import java.time.Instant;

/*
 * comment.
 *  Sendbird 웹훅 - 메시지/답글 수정 이벤트 미러링 커맨드
 *  attachmentUrl: null이면 첨부파일 없음(제거됨 포함)
 */

public record MirrorMessageUpdatedCommand(
        String sendbirdMessageId,
        String content,
        String attachmentUrl,
        Instant eventAt
) {
}
