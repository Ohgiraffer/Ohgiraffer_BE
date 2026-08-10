package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import com.ohgiraffer.notice.domain.repository.NoticeAttachmentRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 첨부 업로드·삭제에서 권한, 개수·크기 상한, 그리고 S3 와 DB 사이의 정합성을 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoticeAttachmentCommandServiceTest {

    private static final Long NOTICE_ID = 10L;
    private static final Long AUTHOR_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long ATTACHMENT_ID = 1L;

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeAttachmentRepository noticeAttachmentRepository;

    @Mock
    private S3FileHandler s3FileHandler;

    private NoticeAttachmentCommandService noticeAttachmentCommandService;

    @BeforeEach
    void setUp() {
        noticeAttachmentCommandService = new NoticeAttachmentCommandService(
                noticeRepository,
                noticeAttachmentRepository,
                s3FileHandler
        );

        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice()));
    }

    @Test
    @DisplayName("공지 등록 전에는 공지 번호 없이 올리고 저장 키만 돌려받는다")
    void uploadBeforeNoticeReturnsKeys() {
        var uploaded = noticeAttachmentCommandService.uploadBeforeNotice(
                List.of(file("안내문.pdf"), file("일정표.png"))
        );

        assertEquals(2, uploaded.size());
        assertTrue(uploaded.get(0).fileKey().startsWith("noticeAttachments/"));

        /*
         * 아직 공지에 이어지지 않았으므로 DB 에는 아무것도 남기지 않는다.
         * 등록 요청에 이 키를 실어 보낼 때 공지와 함께 저장된다.
         */
        verify(noticeAttachmentRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("등록 전 업로드도 형식을 어기면 한 건도 올리지 않는다")
    void uploadBeforeNoticeRejectsUnsupportedType() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeAttachmentCommandService.uploadBeforeNotice(
                        List.of(file("안내문.pdf"), file("악성코드.exe"))
                )
        );

        assertEquals(
                ErrorCode.NOTICE_ATTACHMENT_TYPE_NOT_ALLOWED,
                exception.getErrorCode()
        );
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("작성자는 파일을 여러 개 한 번에 올릴 수 있다")
    void uploadMultipleFiles() {
        when(noticeAttachmentRepository.countByNoticeId(NOTICE_ID))
                .thenReturn(0L);
        when(noticeAttachmentRepository.saveAll(any()))
                .thenReturn(List.of(storedAttachment()));

        noticeAttachmentCommandService.upload(
                NOTICE_ID,
                AUTHOR_ID,
                List.of(file("안내문.pdf"), file("일정표.png"))
        );

        verify(s3FileHandler, times(2)).upload(any(), anyString());
        verify(noticeAttachmentRepository).saveAll(any());
    }

    @Test
    @DisplayName("작성자가 아니면 올릴 수 없고 저장소에 손대지 않는다")
    void uploadRejectsNonAuthor() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeAttachmentCommandService.upload(
                        NOTICE_ID,
                        OTHER_USER_ID,
                        List.of(file("안내문.pdf"))
                )
        );

        assertEquals(ErrorCode.NOTICE_NOT_AUTHOR, exception.getErrorCode());
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("이미 올라간 개수까지 합쳐서 상한을 넘으면 거절한다")
    void uploadRejectsWhenCountExceeded() {
        when(noticeAttachmentRepository.countByNoticeId(NOTICE_ID))
                .thenReturn(4L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeAttachmentCommandService.upload(
                        NOTICE_ID,
                        AUTHOR_ID,
                        List.of(file("1.pdf"), file("2.pdf"))
                )
        );

        /*
         * 4 + 2 = 6 이라 상한 5를 넘는다. 한 건도 올리지 않아야 한다.
         */
        assertEquals(
                ErrorCode.NOTICE_ATTACHMENT_COUNT_EXCEEDED,
                exception.getErrorCode()
        );
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("크기를 넘는 파일이 섞여 있으면 하나도 올리지 않는다")
    void uploadRejectsAllWhenOneFileTooLarge() {
        when(noticeAttachmentRepository.countByNoticeId(NOTICE_ID))
                .thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeAttachmentCommandService.upload(
                        NOTICE_ID,
                        AUTHOR_ID,
                        List.of(file("작은파일.pdf"), tooLargeFile())
                )
        );

        /*
         * 검사를 업로드보다 먼저 끝내는 이유가 이것이다. 올리면서 검사하면
         * 첫 파일은 이미 S3 에 올라간 뒤 두 번째에서 걸린다.
         */
        assertEquals(
                ErrorCode.NOTICE_ATTACHMENT_TOO_LARGE,
                exception.getErrorCode()
        );
        verify(s3FileHandler, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("DB 저장이 실패하면 이미 올린 S3 객체를 지운다")
    void uploadCleansUpS3WhenSaveFails() {
        when(noticeAttachmentRepository.countByNoticeId(NOTICE_ID))
                .thenReturn(0L);
        when(noticeAttachmentRepository.saveAll(any()))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        assertThrows(
                RuntimeException.class,
                () -> noticeAttachmentCommandService.upload(
                        NOTICE_ID,
                        AUTHOR_ID,
                        List.of(file("1.pdf"), file("2.pdf"))
                )
        );

        /*
         * S3 는 트랜잭션에 참여하지 않아 롤백으로 되돌아가지 않는다.
         * 올린 만큼 직접 지워야 주인 없는 객체가 남지 않는다.
         */
        verify(s3FileHandler, times(2)).delete(anyString());
    }

    @Test
    @DisplayName("삭제는 DB 행과 S3 객체를 함께 지운다")
    void deleteRemovesRowAndObject() {
        when(noticeAttachmentRepository.findById(ATTACHMENT_ID))
                .thenReturn(Optional.of(storedAttachment()));

        noticeAttachmentCommandService.delete(
                NOTICE_ID,
                ATTACHMENT_ID,
                AUTHOR_ID
        );

        verify(noticeAttachmentRepository).deleteById(ATTACHMENT_ID);
        verify(s3FileHandler).delete("noticeAttachments/10/uuid.pdf");
    }

    @Test
    @DisplayName("다른 공지의 첨부는 지울 수 없다")
    void deleteRejectsAttachmentOfAnotherNotice() {
        when(noticeAttachmentRepository.findById(ATTACHMENT_ID))
                .thenReturn(Optional.of(NoticeAttachment.restore(
                        ATTACHMENT_ID,
                        999L,
                        "noticeAttachments/999/uuid.pdf",
                        "남의공지.pdf",
                        100L,
                        "application/pdf",
                        Instant.now()
                )));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeAttachmentCommandService.delete(
                        NOTICE_ID,
                        ATTACHMENT_ID,
                        AUTHOR_ID
                )
        );

        assertEquals(
                ErrorCode.NOTICE_ATTACHMENT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(noticeAttachmentRepository, never()).deleteById(any());
    }

    private Notice notice() {
        return Notice.restore(
                NOTICE_ID,
                AUTHOR_ID,
                1L,
                "8월 휴강 안내",
                "<p>본문</p>",
                false,
                true,
                Instant.now(),
                Instant.now()
        );
    }

    private NoticeAttachment storedAttachment() {
        return NoticeAttachment.restore(
                ATTACHMENT_ID,
                NOTICE_ID,
                "noticeAttachments/10/uuid.pdf",
                "안내문.pdf",
                1024L,
                "application/pdf",
                Instant.now()
        );
    }

    private MultipartFile file(String name) {
        return new MockMultipartFile(
                "files",
                name,
                "application/pdf",
                "내용".getBytes()
        );
    }

    private MultipartFile tooLargeFile() {
        return new MockMultipartFile(
                "files",
                "큰파일.pdf",
                "application/pdf",
                new byte[(int) NoticeAttachment.MAX_FILE_SIZE_BYTES + 1]
        );
    }
}
