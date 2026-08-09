package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3UrlResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 본문 이미지는 첨부파일과 달리 만료되지 않는 주소를 돌려줘야 한다.
 */
@ExtendWith(MockitoExtension.class)
class NoticeImageCommandServiceTest {

    private static final String PRESIGNED_URL =
            "https://ohgiraffer-media.s3.ap-northeast-2.amazonaws.com/"
                    + "noticeImages/uuid.png?X-Amz-Signature=abc";

    @Mock
    private S3FileHandler s3FileHandler;

    @Mock
    private S3UrlResolver s3UrlResolver;

    private NoticeImageCommandService noticeImageCommandService;

    @BeforeEach
    void setUp() {
        noticeImageCommandService = new NoticeImageCommandService(
                s3FileHandler,
                s3UrlResolver
        );
    }

    @Test
    @DisplayName("올리면 저장소 주소가 아니라 서버 경로를 돌려준다")
    void uploadReturnsServerPath() {
        String imageUrl = noticeImageCommandService.upload(image("사진.png"));

        /*
         * 저장소 주소를 그대로 주면 만료되거나 버킷을 공개해야 한다.
         * 서버 경로는 만료되지 않고 버킷도 비공개로 둘 수 있다.
         */
        assertTrue(imageUrl.startsWith("/notices/images/"));
        assertTrue(imageUrl.endsWith(".png"));
    }

    @Test
    @DisplayName("공지 번호 없이 올라가므로 키는 공지별로 묶이지 않는다")
    void uploadUsesNoticeImagePrefix() {
        noticeImageCommandService.upload(image("사진.png"));

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);
        verify(s3FileHandler).upload(any(), keyCaptor.capture());

        assertTrue(keyCaptor.getValue().startsWith("noticeImages/"));
        assertTrue(keyCaptor.getValue().endsWith(".png"));
    }

    @Test
    @DisplayName("조회 경로는 저장소 주소로 바꿔 준다")
    void resolveUrlReturnsStorageUrl() {
        when(s3UrlResolver.resolve(anyString())).thenReturn(PRESIGNED_URL);

        String url = noticeImageCommandService.resolveUrl(
                "3f2504e0-4f89-41d3-9a0c-0305e82c3301.png");

        assertEquals(PRESIGNED_URL, url);
        verify(s3UrlResolver)
                .resolve("noticeImages/3f2504e0-4f89-41d3-9a0c-0305e82c3301.png");
    }

    @Test
    @DisplayName("다른 폴더를 넘겨다보는 파일명은 거절한다")
    void resolveUrlRejectsPathTraversal() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeImageCommandService.resolveUrl("../profileImg/1")
        );

        /*
         * 이 값이 그대로 저장소 키에 붙는다. 막지 않으면 남의 폴더 주소를 받아낼 수 있다.
         */
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(s3UrlResolver, never()).resolve(anyString());
    }

    @Test
    @DisplayName("JPG PNG 가 아니면 올리지 않는다")
    void uploadRejectsUnsupportedType() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeImageCommandService.upload(image("문서.pdf"))
        );

        assertEquals(
                ErrorCode.NOTICE_IMAGE_TYPE_NOT_ALLOWED,
                exception.getErrorCode()
        );
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("확장자를 바꿔 붙여도 대소문자와 무관하게 판단한다")
    void uploadAcceptsUppercaseExtension() {
        noticeImageCommandService.upload(image("사진.PNG"));

        verify(s3FileHandler).upload(any(), anyString());
    }

    @Test
    @DisplayName("크기를 넘으면 올리지 않는다")
    void uploadRejectsTooLargeImage() {
        MultipartFile tooLarge = new MockMultipartFile(
                "image",
                "큰사진.png",
                "image/png",
                new byte[5 * 1024 * 1024 + 1]
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeImageCommandService.upload(tooLarge)
        );

        assertEquals(
                ErrorCode.NOTICE_ATTACHMENT_TOO_LARGE,
                exception.getErrorCode()
        );
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    private MultipartFile image(String name) {
        return new MockMultipartFile(
                "image",
                name,
                "image/png",
                "이미지".getBytes()
        );
    }
}
