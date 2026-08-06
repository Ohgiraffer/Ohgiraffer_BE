package com.ohgiraffer.notice.application.command;

/**
 * 공지 수정 입력값.
 *
 * <p>editorId 는 클라이언트가 지정하지 않고 서버가 로그인 사용자로 채운다.
 * 작성자 본인인지 확인하는 데 쓴다.
 */
public record UpdateNoticeCommand(
        Long noticeId,
        Long editorId,
        Long categoryId,
        String title,
        String content,
        boolean pinned,
        boolean visibleToTrainee
) {
}
