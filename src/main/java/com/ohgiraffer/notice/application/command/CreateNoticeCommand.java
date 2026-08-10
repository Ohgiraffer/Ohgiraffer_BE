package com.ohgiraffer.notice.application.command;

import java.util.List;

/**
 * 공지 등록 요청을 표현하는 애플리케이션 계층 입력값.
 *
 * <p>authorId 는 클라이언트가 지정하지 않고 서버가 채운다.
 *
 * <p>{@code attachments} 는 미리 올려 둔 파일들이다. 공지와 같은 트랜잭션에서 저장돼
 * 둘 중 하나만 남는 일이 없다.
 */
public record CreateNoticeCommand(
        Long authorId,
        Long categoryId,
        String title,
        String content,
        boolean pinned,
        boolean visibleToTrainee,
        List<NoticeAttachmentCommand> attachments
) {

    public CreateNoticeCommand {
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}
