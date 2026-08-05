package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.command.UpdateNoticeCommand;
import com.ohgiraffer.notice.application.query.NoticeConfirmationView;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long NOTICE_ID = 10L;
    private static final Long CATEGORY_ID = 1L;
    private static final String TITLE = "8월 특강 안내";
    private static final String CONTENT = "<p>본문입니다.</p>";

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeCategoryRepository noticeCategoryRepository;

    @Mock
    private NoticeConfirmationRepository noticeConfirmationRepository;

    private NoticeCommandService noticeCommandService;

    @BeforeEach
    void setUp() {
        noticeCommandService = new NoticeCommandService(
                noticeRepository,
                noticeCategoryRepository,
                noticeConfirmationRepository
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

    @Test
    @DisplayName("작성자 본인이면 공지를 수정할 수 있다")
    void updateNotice() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID)));
        when(noticeCategoryRepository.existsById(2L)).thenReturn(true);
        when(noticeRepository.update(any(Notice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        noticeCommandService.update(updateCommand(AUTHOR_ID, 2L, "고친 제목"));

        ArgumentCaptor<Notice> captor =
                ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).update(captor.capture());

        Notice passed = captor.getValue();
        assertEquals(NOTICE_ID, passed.getId());
        assertEquals(2L, passed.getCategoryId());
        assertEquals("고친 제목", passed.getTitle());
        assertEquals(AUTHOR_ID, passed.getAuthorId());
    }

    @Test
    @DisplayName("작성자가 아니면 수정할 수 없고 저장소를 건드리지 않는다")
    void updateFailsWhenNotAuthor() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.update(
                        updateCommand(OTHER_USER_ID, CATEGORY_ID, TITLE))
        );

        assertEquals(ErrorCode.NOTICE_NOT_AUTHOR, exception.getErrorCode());
        verify(noticeRepository, never()).update(any(Notice.class));
    }

    @Test
    @DisplayName("없는 공지는 수정할 수 없다")
    void updateFailsWhenNoticeMissing() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.update(
                        updateCommand(AUTHOR_ID, CATEGORY_ID, TITLE))
        );

        assertEquals(ErrorCode.NOTICE_NOT_FOUND, exception.getErrorCode());
        verify(noticeRepository, never()).update(any(Notice.class));
    }

    @Test
    @DisplayName("작성자 본인이면 공지를 삭제할 수 있다")
    void deleteNotice() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID)));

        noticeCommandService.delete(NOTICE_ID, AUTHOR_ID);

        verify(noticeRepository).deleteById(NOTICE_ID);
    }

    @Test
    @DisplayName("작성자가 아니면 삭제할 수 없고 저장소를 건드리지 않는다")
    void deleteFailsWhenNotAuthor() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.delete(NOTICE_ID, OTHER_USER_ID)
        );

        assertEquals(ErrorCode.NOTICE_NOT_AUTHOR, exception.getErrorCode());
        verify(noticeRepository, never()).deleteById(NOTICE_ID);
    }

    @Test
    @DisplayName("필수 공지는 확인 처리하면 확인 기록을 남긴다")
    void confirmMandatoryNotice() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID, true)));

        noticeCommandService.confirm(NOTICE_ID, OTHER_USER_ID);

        verify(noticeConfirmationRepository).confirm(NOTICE_ID, OTHER_USER_ID);
    }

    @Test
    @DisplayName("확인 처리 결과로 갱신된 인원수를 돌려준다")
    void confirmReturnsUpdatedCount() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID, true)));
        when(noticeConfirmationRepository.countBy(NOTICE_ID))
                .thenReturn(12L);

        NoticeConfirmationView view =
                noticeCommandService.confirm(NOTICE_ID, OTHER_USER_ID);

        assertEquals(NOTICE_ID, view.noticeId());
        assertEquals(12L, view.confirmationCount());
        assertTrue(view.confirmedByMe());
    }

    @Test
    @DisplayName("인원수는 확인 기록을 남긴 뒤에 세어 방금 확인한 건이 반영된다")
    void confirmCountsAfterSaving() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID, true)));

        noticeCommandService.confirm(NOTICE_ID, OTHER_USER_ID);

        InOrder inOrder = inOrder(noticeConfirmationRepository);
        inOrder.verify(noticeConfirmationRepository)
                .confirm(NOTICE_ID, OTHER_USER_ID);
        inOrder.verify(noticeConfirmationRepository).countBy(NOTICE_ID);
    }

    @Test
    @DisplayName("작성자가 아닌 사람도 필수 공지를 확인할 수 있다")
    void confirmDoesNotRequireAuthor() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID, true)));

        noticeCommandService.confirm(NOTICE_ID, AUTHOR_ID);

        verify(noticeConfirmationRepository).confirm(NOTICE_ID, AUTHOR_ID);
    }

    @Test
    @DisplayName("일반 공지는 확인 처리할 수 없고 기록도 남기지 않는다")
    void confirmFailsWhenNotMandatory() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(stored(AUTHOR_ID, false)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.confirm(NOTICE_ID, OTHER_USER_ID)
        );

        assertEquals(ErrorCode.NOTICE_NOT_MANDATORY, exception.getErrorCode());
        verify(noticeConfirmationRepository, never())
                .confirm(any(), any());
    }

    @Test
    @DisplayName("없는 공지를 확인하면 404로 알린다")
    void confirmFailsWhenNoticeMissing() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCommandService.confirm(NOTICE_ID, OTHER_USER_ID)
        );

        assertEquals(ErrorCode.NOTICE_NOT_FOUND, exception.getErrorCode());
        verify(noticeConfirmationRepository, never())
                .confirm(any(), any());
    }

    private UpdateNoticeCommand updateCommand(
            Long editorId,
            Long categoryId,
            String title
    ) {
        return new UpdateNoticeCommand(
                NOTICE_ID,
                editorId,
                categoryId,
                title,
                CONTENT,
                false,
                true
        );
    }

    private Notice stored(Long authorId) {
        return stored(authorId, false);
    }

    private Notice stored(Long authorId, boolean mandatory) {
        Instant now = Instant.parse("2026-08-04T03:00:00Z");

        return Notice.restore(
                NOTICE_ID,
                authorId,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                mandatory,
                true,
                now,
                now
        );
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
