package com.ohgiraffer.notice.application.command;

/**
 * 공지 등록 요청을 표현하는 애플리케이션 계층 입력값.
 *
 * <p>authorId 는 클라이언트가 지정하지 않고 서버가 채운다.
 */
public record CreateNoticeCommand(
        Long authorId,
        Long categoryId,
        String title,
        String content,
        boolean mandatory,
        boolean visibleToTrainee
) {
}
