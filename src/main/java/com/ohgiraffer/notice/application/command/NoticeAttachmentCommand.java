package com.ohgiraffer.notice.application.command;

/**
 * 공지를 저장할 때 함께 붙일 첨부파일.
 *
 * <p>파일 자체는 이미 저장소에 올라가 있고 여기에는 그 키와 표시용 값만 담긴다.
 * 등록 화면에서 파일을 고르는 순간 올려 두었다가, 등록 버튼을 누를 때 공지와 함께 보낸다.
 *
 * <p>이렇게 나눈 것은 화면의 등록 버튼이 하나이기 때문이다. 공지를 먼저 저장하고 첨부를
 * 따로 올리면 그 사이에 실패했을 때 첨부 없는 공지가 남고, 사용자는 실패한 줄 알고
 * 다시 눌러 공지를 두 번 만들게 된다.
 */
public record NoticeAttachmentCommand(
        String fileKey,
        String fileName,
        long fileSizeBytes,
        String fileType
) {
}
