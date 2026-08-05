package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCategoryCommand;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeCategoryCommandServiceTest {

    private static final Long CATEGORY_ID = 2L;

    @Mock
    private NoticeCategoryRepository noticeCategoryRepository;

    @Mock
    private NoticeRepository noticeRepository;

    private NoticeCategoryCommandService noticeCategoryCommandService;

    @BeforeEach
    void setUp() {
        noticeCategoryCommandService = new NoticeCategoryCommandService(
                noticeCategoryRepository,
                noticeRepository
        );
    }

    @Test
    @DisplayName("카테고리를 등록하면 저장소에 전달하고 저장된 카테고리를 돌려준다")
    void createCategory() {
        when(noticeCategoryRepository.save(any(NoticeCategory.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        NoticeCategory result = noticeCategoryCommandService.create(
                new CreateNoticeCategoryCommand("기타")
        );

        ArgumentCaptor<NoticeCategory> captor =
                ArgumentCaptor.forClass(NoticeCategory.class);
        verify(noticeCategoryRepository).save(captor.capture());

        assertEquals("기타", captor.getValue().getName());
        assertEquals(CATEGORY_ID, result.getId());
    }

    @Test
    @DisplayName("이름 앞뒤 공백은 떼고 저장한다")
    void createTrimsName() {
        when(noticeCategoryRepository.save(any(NoticeCategory.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        noticeCategoryCommandService.create(
                new CreateNoticeCategoryCommand("  기타  ")
        );

        ArgumentCaptor<NoticeCategory> captor =
                ArgumentCaptor.forClass(NoticeCategory.class);
        verify(noticeCategoryRepository).save(captor.capture());

        assertEquals("기타", captor.getValue().getName());
    }

    @Test
    @DisplayName("공백을 뗀 이름으로 중복을 검사한다")
    void createChecksDuplicateWithTrimmedName() {
        when(noticeCategoryRepository.existsByName("수업"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCategoryCommandService.create(
                        new CreateNoticeCategoryCommand("  수업  "))
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_DUPLICATE_NAME,
                exception.getErrorCode()
        );
        verify(noticeCategoryRepository, never())
                .save(any(NoticeCategory.class));
    }

    @Test
    @DisplayName("이름이 비면 중복 검사도 하지 않고 막는다")
    void createFailsWhenNameBlank() {
        assertThrows(
                BusinessException.class,
                () -> noticeCategoryCommandService.create(
                        new CreateNoticeCategoryCommand("   "))
        );

        verify(noticeCategoryRepository, never()).existsByName(any());
        verify(noticeCategoryRepository, never())
                .save(any(NoticeCategory.class));
    }

    @Test
    @DisplayName("사용하는 공지가 없으면 카테고리를 삭제한다")
    void deleteCategory() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(true);
        when(noticeRepository.countByCategoryId(CATEGORY_ID))
                .thenReturn(0L);

        noticeCategoryCommandService.delete(CATEGORY_ID);

        verify(noticeCategoryRepository).deleteById(CATEGORY_ID);
    }

    @Test
    @DisplayName("공지가 사용 중이면 삭제하지 않고 몇 건인지 알려준다")
    void deleteFailsWhenInUse() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(true);
        when(noticeRepository.countByCategoryId(CATEGORY_ID))
                .thenReturn(3L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCategoryCommandService.delete(CATEGORY_ID)
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_IN_USE,
                exception.getErrorCode()
        );
        assertTrue(exception.getMessage().contains("3건"));
        verify(noticeCategoryRepository, never()).deleteById(CATEGORY_ID);
    }

    @Test
    @DisplayName("없는 카테고리는 삭제할 수 없고 사용 여부도 세지 않는다")
    void deleteFailsWhenCategoryMissing() {
        when(noticeCategoryRepository.existsById(CATEGORY_ID))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCategoryCommandService.delete(CATEGORY_ID)
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(noticeRepository, never()).countByCategoryId(any());
        verify(noticeCategoryRepository, never()).deleteById(CATEGORY_ID);
    }

    private NoticeCategory saved(NoticeCategory category) {
        return NoticeCategory.restore(CATEGORY_ID, category.getName());
    }
}
