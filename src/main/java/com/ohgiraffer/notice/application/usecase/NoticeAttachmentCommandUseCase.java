package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeAttachmentCommandUseCase {

    /**
     * 공지를 저장하기 전에 파일만 먼저 올린다. 등록 화면에서 파일을 고르는 순간 호출한다.
     *
     * <p>DB 에는 아무것도 남기지 않는다. 저장소에만 올려 두고, 돌려준 키를 공지 등록 요청에
     * 함께 실어 보내면 그때 공지와 이어진다.
     *
     * @return 저장소 키와 화면 표시에 쓸 값
     */
    List<UploadedNoticeAttachment> uploadBeforeNotice(List<MultipartFile> files);

    /**
     * 아직 공지에 이어지지 않은 파일.
     */
    record UploadedNoticeAttachment(
            String fileKey,
            String fileName,
            long fileSizeBytes,
            String fileType
    ) {
    }

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
