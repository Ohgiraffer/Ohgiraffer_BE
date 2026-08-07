package com.ohgiraffer.notice.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/**
 * 공지에 붙는 첨부파일. JPA와 무관한 순수 객체다.
 *
 * <p>{@code fileKey} 는 S3 객체 키다. 테이블 컬럼명은 {@code file_url} 이지만 URL 을 넣지 않는다.
 * 내려주는 주소는 만료 시간이 있는 presigned URL 이라 저장해 두면 곧 쓸모없어지기 때문이다.
 * 주소는 조회할 때마다 키로부터 새로 만든다. 제출물도 같은 이유로 키를 저장한다.
 */
public class NoticeAttachment {

    /**
     * 파일 하나의 최대 크기.
     *
     * <p>전역 multipart 설정(110MB)은 제출물의 과제 파일 기준이라 공지 첨부에는 지나치게 크다.
     * 공지 첨부는 안내문·이미지가 대부분이라 여기서 따로 좁힌다.
     */
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    /**
     * 공지 하나에 붙일 수 있는 파일 수.
     *
     * <p>총 용량을 따로 세지 않는 것은 이 상한이 이미 총량을 묶기 때문이다(5 × 10MB).
     * 누적 합계를 검사하면 조회가 늘고, 동시에 올라온 두 요청이 각각 여유를 확인해
     * 함께 통과하는 구멍도 생긴다.
     */
    public static final int MAX_COUNT_PER_NOTICE = 5;

    /**
     * 허용 확장자. 등록 화면의 안내 문구(PDF, DOCX, XLS, HWP, JPG, PNG)에 맞춘다.
     *
     * <p>같은 계열의 구버전·신버전 확장자를 함께 받는다. 화면에 XLS 로 적혀 있어도
     * 요즘 엑셀이 저장하는 것은 XLSX 라, 글자 그대로만 받으면 대부분이 막힌다.
     *
     * <p>브라우저가 보내는 content-type 이 아니라 확장자로 판단한다.
     * content-type 은 요청하는 쪽이 정하는 값이라 그대로 믿을 수 없다.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf",
            "doc", "docx",
            "xls", "xlsx",
            "hwp", "hwpx",
            "jpg", "jpeg", "png"
    );

    private static final int FILE_KEY_MAX_LENGTH = 500;
    private static final int FILE_NAME_MAX_LENGTH = 255;
    private static final int FILE_TYPE_MAX_LENGTH = 20;

    private final Long id;
    private final Long noticeId;
    private final String fileKey;
    private final String fileName;
    private final Long fileSizeBytes;
    private final String fileType;
    private final Instant uploadedAt;

    private NoticeAttachment(
            Long id,
            Long noticeId,
            String fileKey,
            String fileName,
            Long fileSizeBytes,
            String fileType,
            Instant uploadedAt
    ) {
        this.id = id;
        this.noticeId = noticeId;
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
    }

    public static NoticeAttachment create(
            Long noticeId,
            String fileKey,
            String fileName,
            long fileSizeBytes,
            String fileType
    ) {
        validateNoticeId(noticeId);
        validateFileKey(fileKey);
        validateFileSize(fileSizeBytes);

        return new NoticeAttachment(
                null,
                noticeId,
                fileKey,
                truncate(fileName, FILE_NAME_MAX_LENGTH),
                fileSizeBytes,
                truncate(fileType, FILE_TYPE_MAX_LENGTH),
                Instant.now()
        );
    }

    /**
     * 저장소에서 읽어온 값으로 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static NoticeAttachment restore(
            Long id,
            Long noticeId,
            String fileKey,
            String fileName,
            Long fileSizeBytes,
            String fileType,
            Instant uploadedAt
    ) {
        return new NoticeAttachment(
                id,
                noticeId,
                fileKey,
                fileName,
                fileSizeBytes,
                fileType,
                uploadedAt
        );
    }

    /**
     * 이 첨부가 해당 공지의 것인지 여부.
     *
     * <p>경로에 공지와 첨부 식별자가 함께 오므로, 남의 공지 번호로 다른 공지의 첨부를
     * 지우려는 요청을 걸러내기 위해 확인한다.
     */
    public boolean belongsTo(Long noticeId) {
        return this.noticeId != null && this.noticeId.equals(noticeId);
    }

    /**
     * 업로드된 파일 크기가 허용치를 넘는지 미리 확인한다.
     *
     * <p>S3 에 올리기 전에 걸러내기 위해 정적 메서드로 열어 둔다.
     * 올린 뒤에 거절하면 지워야 할 객체가 남는다.
     */
    public static void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "빈 파일은 첨부할 수 없습니다."
            );
        }

        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(
                    ErrorCode.NOTICE_ATTACHMENT_TOO_LARGE,
                    "첨부파일 하나는 "
                            + (MAX_FILE_SIZE_BYTES / 1024 / 1024)
                            + "MB 를 넘을 수 없습니다."
            );
        }
    }

    /**
     * 허용하지 않는 형식인지 미리 확인한다.
     *
     * <p>크기 검사와 같은 이유로 정적 메서드다. S3 에 올린 뒤 거절하면 지워야 할 객체가 남는다.
     */
    public static void validateFileType(String originalFileName) {
        if (!ALLOWED_EXTENSIONS.contains(extension(originalFileName))) {
            throw new BusinessException(
                    ErrorCode.NOTICE_ATTACHMENT_TYPE_NOT_ALLOWED,
                    "PDF, DOCX, XLS, HWP, JPG, PNG 만 첨부할 수 있습니다."
            );
        }
    }

    private static String extension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "";
        }

        int dotIndex = originalFileName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return "";
        }

        return originalFileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private static void validateNoticeId(Long noticeId) {
        if (noticeId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "첨부할 공지 정보가 필요합니다."
            );
        }
    }

    private static void validateFileKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키가 필요합니다."
            );
        }

        if (fileKey.length() > FILE_KEY_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키가 너무 깁니다."
            );
        }
    }

    /**
     * 컬럼 길이를 넘는 값은 잘라서 담는다.
     *
     * <p>파일명이나 형식이 길다는 이유로 업로드를 막지는 않는다. 사용자가 고칠 수 없는 값이고,
     * 잘려도 파일 자체는 온전하기 때문이다. 저장 시점에 DB 가 거절하는 것보다 낫다.
     */
    private static String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        return trimmed.substring(0, maxLength);
    }

    public Long getId() {
        return id;
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getFileType() {
        return fileType;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
