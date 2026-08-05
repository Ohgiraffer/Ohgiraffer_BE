package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final String TITLE = "8월 특강 안내";
    private static final String CONTENT = "<p>본문입니다.</p>";

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeCategoryRepository noticeCategoryRepository;

    private NoticeCommandService noticeCommandService;

    @BeforeEach
    void setUp() {
        noticeCommandService = new NoticeCommandService(
                noticeRepository,
                noticeCategoryRepository
        );
    }

    @Test
    @DisplayName("공지를 등록하면 저장소에 전달하고 저장된 공지를 돌려준다")
    void createNotice() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(true);
        when(noticeRepository.save(any(Notice.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        Notice result = noticeCommandService.create(command(false, true));

        ArgumentCaptor<Notice> captor =
                ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());

        Notice passed = captor.getValue();
        assertEquals(AUTHOR_ID, passed.getAuthorId());
        assertEquals(CATEGORY_ID, passed.getCategoryId());
        assertEquals(TITLE, passed.getTitle());
        assertEquals(CONTENT, passed.getContent());

        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리면 저장하지 않고 404로 알린다")
    void createFailsWhenCategoryMissing() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.create(command(false, true))
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    @DisplayName("업무 규칙을 어기면 저장소를 호출하지 않는다")
    void createDoesNotTouchRepositoryWhenDomainRuleFails() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(true);

        CreateNoticeCommand invalidCommand = new CreateNoticeCommand(
                AUTHOR_ID,
                CATEGORY_ID,
                "   ",
                CONTENT,
                false,
                true
        );

        assertThrows(
                BusinessException.class,
                () -> noticeCommandService.create(invalidCommand)
        );

        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    @DisplayName("필수 공지로 등록하면 확인 대상으로 저장된다")
    void createMandatoryNotice() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(true);
        when(noticeRepository.save(any(Notice.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        noticeCommandService.create(command(true, false));

        ArgumentCaptor<Notice> captor =
                ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());

        Notice passed = captor.getValue();
        assertTrue(passed.isMandatory());
        assertTrue(passed.requiresConfirmation());
        assertEquals(false, passed.isVisibleToTrainee());
    }

    private CreateNoticeCommand command(
            boolean mandatory,
            boolean visibleToTrainee
    ) {
        return new CreateNoticeCommand(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                mandatory,
                visibleToTrainee
        );
    }

    /**
     * 저장 후 식별자와 시각이 채워진 상태를 흉내낸다.
     */
    private Notice saved(Notice notice) {
        Instant now = Instant.parse("2026-08-04T03:00:00Z");

        return Notice.restore(
                100L,
                notice.getAuthorId(),
                notice.getCategoryId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isMandatory(),
                notice.isVisibleToTrainee(),
                now,
                now
        );
    }
}
