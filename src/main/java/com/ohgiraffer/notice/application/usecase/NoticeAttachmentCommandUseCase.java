package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeAttachmentCommandUseCase {

    /**
     * 공지에 파일을 첨부한다. 작성자만 할 수 있다.
     *
     * <p>한 요청의 파일은 전부 성공하거나 전부 실패한다.
     */
    List<NoticeAttachment> upload(
            Long noticeId,
            Long requesterId,
            List<MultipartFile> files
    );

    void delete(
            Long noticeId,
            Long noticeAttachmentId,
            Long requesterId
    );
}
