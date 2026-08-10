package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.notice.application.usecase.NoticeImageCommandUseCase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 공지 본문에 넣는 이미지.
 *
 * <p>첨부파일과 세 가지가 다르다.
 *
 * <ol>
 *   <li>공지 번호를 받지 않는다. 글을 쓰는 중이라 아직 공지가 없다.</li>
 *   <li>DB 에 행을 남기지 않는다. 어떤 이미지를 쓰는지는 본문 자체가 알고 있다.</li>
 *   <li>주소가 만료되면 안 된다. 본문에 그대로 저장돼 계속 표시되기 때문이다.</li>
 * </ol>
 *
 * <p>세 번째 때문에 presigned URL 을 본문에 넣을 수 없다. 5분짜리 주소를 박으면 곧 깨진다.
 * 그렇다고 S3 를 공개하지도 않는다. 대신 <b>서버를 거치는 주소</b>를 돌려주고,
 * 그 경로로 요청이 오면 그때 만든 presigned URL 로 리다이렉트한다.
 * 본문에 저장되는 주소는 만료되지 않고, 버킷은 비공개로 남는다.
 */
@Service
public class NoticeImageCommandService implements NoticeImageCommandUseCase {

    /**
     * 본문 이미지 한 장의 최대 크기.
     *
     * <p>첨부파일보다 작게 잡는다. 본문 이미지는 화면에 바로 그려지므로 크면 공지가 느려지고,
     * 여러 장이 한 본문에 들어갈 수 있다.
     */
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    /**
     * 허용 확장자. 화면 안내의 JPG / PNG 에 맞춘다.
     *
     * <p>브라우저가 보내는 content-type 대신 확장자로 판단한다. content-type 은
     * 요청하는 쪽이 정하는 값이라 그대로 믿을 수 없다.
     */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    /** 본문에 저장되는 주소의 접두어. 이 경로로 오면 서버가 S3 로 넘겨준다. */
    public static final String IMAGE_PATH_PREFIX = "/notices/images/";

    /** S3 키에서 이 prefix 를 뗀 나머지가 경로의 파일명이 된다. */
    public static final String S3_KEY_PREFIX = "noticeImages/";

    /** 우리가 만드는 파일명: UUID + 확장자. 그 밖의 모양은 받지 않는다. */
    private static final Pattern FILE_NAME_PATTERN =
            Pattern.compile("^[a-fA-F0-9-]{36}\\.[a-z0-9]{1,20}$");

    private final S3FileHandler s3FileHandler;
    private final S3UrlResolver s3UrlResolver;

    public NoticeImageCommandService(
            S3FileHandler s3FileHandler,
            S3UrlResolver s3UrlResolver
    ) {
        this.s3FileHandler = s3FileHandler;
        this.s3UrlResolver = s3UrlResolver;
    }

    @Override
    public String upload(MultipartFile image) {
        validate(image);

        String key = S3KeyGenerator.noticeImageKey(image.getOriginalFilename());

        s3FileHandler.upload(image, key);

        /*
         * S3 주소가 아니라 서버 경로를 돌려준다. S3 주소를 그대로 주면 만료되거나
         * 버킷을 공개해야 하는데, 이 경로는 만료되지 않고 버킷도 비공개로 둘 수 있다.
         *
         * 상대 경로인 것은 환경마다 서버 주소가 다르기 때문이다. 화면에서 API 기본 주소를
         * 앞에 붙여 쓰면 된다.
         */
        return IMAGE_PATH_PREFIX + key.substring(S3_KEY_PREFIX.length());
    }

    @Override
    public String resolveUrl(String fileName) {
        validateFileName(fileName);

        return s3UrlResolver.resolve(S3_KEY_PREFIX + fileName);
    }

    /**
     * 경로로 들어온 파일명이 우리가 만든 모양인지 확인한다.
     *
     * <p>이 값은 그대로 저장소 키에 이어 붙는다. 검사하지 않으면 {@code ../profileImg/1} 같은
     * 값을 보내 다른 폴더의 파일 주소를 받아낼 수 있다.
     * 우리가 만드는 이름은 UUID 와 확장자뿐이므로 그 모양만 받는다.
     */
    private void validateFileName(String fileName) {
        if (fileName == null || !FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "올바른 이미지 경로가 아닙니다."
            );
        }
    }

    private void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "업로드할 이미지가 필요합니다."
            );
        }

        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(
                    ErrorCode.NOTICE_ATTACHMENT_TOO_LARGE,
                    "본문 이미지는 "
                            + (MAX_IMAGE_SIZE_BYTES / 1024 / 1024)
                            + "MB 를 넘을 수 없습니다."
            );
        }

        if (!ALLOWED_EXTENSIONS.contains(extension(image.getOriginalFilename()))) {
            throw new BusinessException(
                    ErrorCode.NOTICE_IMAGE_TYPE_NOT_ALLOWED,
                    "본문 이미지는 JPG, PNG 만 넣을 수 있습니다."
            );
        }
    }

    private String extension(String originalFileName) {
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
}
