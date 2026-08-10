package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 공지 첨부파일.
 *
 * <p>{@code file_url} 컬럼에는 URL 이 아니라 S3 객체 키가 들어간다. 컬럼명은 baseline 스키마라
 * 바꾸지 않았다. presigned URL 은 만료되므로 저장하지 않고, 조회할 때 키로부터 만든다.
 *
 * <p>{@code BaseTimeEntity} 를 상속하지 않는 것은 이 테이블에 created_at / updated_at 이 없고
 * 업로드 시각 하나만 있기 때문이다. 첨부는 수정 개념이 없어 그것으로 충분하다.
 */
@Entity
@Table(name = "notice_attachment")
public class NoticeAttachmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_attachment_id")
    private Long id;

    @Column(name = "notice_id", nullable = false)
    private Long noticeId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileKey;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    protected NoticeAttachmentJpaEntity() {
    }

    private NoticeAttachmentJpaEntity(NoticeAttachment attachment) {
        this.noticeId = attachment.getNoticeId();
        this.fileKey = attachment.getFileKey();
        this.fileName = attachment.getFileName();
        this.fileSizeBytes = attachment.getFileSizeBytes();
        this.fileType = attachment.getFileType();
        this.uploadedAt = attachment.getUploadedAt();
    }

    public static NoticeAttachmentJpaEntity from(NoticeAttachment attachment) {
        return new NoticeAttachmentJpaEntity(attachment);
    }

    public NoticeAttachment toDomain() {
        return NoticeAttachment.restore(
                id,
                noticeId,
                fileKey,
                fileName,
                fileSizeBytes,
                fileType,
                uploadedAt
        );
    }
}
