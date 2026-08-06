package com.ohgiraffer.notice.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoticeTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final String TITLE = "8월 특강 안내";
    private static final String CONTENT = "<p>본문입니다.</p>";

    @Test
    @DisplayName("공지를 생성하면 저장 전이라 식별자와 시각이 비어 있다")
    void createReturnsUnsavedNotice() {
        Notice notice = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                true
        );

        assertNull(notice.getId());
        assertNull(notice.getCreatedAt());
        assertNull(notice.getUpdatedAt());
        assertEquals(AUTHOR_ID, notice.getAuthorId());
        assertEquals(CATEGORY_ID, notice.getCategoryId());
        assertEquals(TITLE, notice.getTitle());
        assertEquals(CONTENT, notice.getContent());
        assertTrue(notice.isVisibleToTrainee());
    }

    @Test
    @DisplayName("제목 앞뒤 공백은 제거하고 저장한다")
    void createTrimsTitle() {
        Notice notice = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                "   8월 특강 안내   ",
                CONTENT,
                false,
                true
        );

        assertEquals("8월 특강 안내", notice.getTitle());
    }

    @Test
    @DisplayName("제목이 비어 있으면 생성할 수 없다")
    void createRejectsBlankTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Notice.create(
                        AUTHOR_ID,
                        CATEGORY_ID,
                        "   ",
                        CONTENT,
                        false,
                        true
                )
        );

        assertEquals(
                ErrorCode.INVALID_INPUT_VALUE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("제목이 255자를 넘으면 생성할 수 없다")
    void createRejectsTooLongTitle() {
        String tooLongTitle = "가".repeat(256);

        assertThrows(
                BusinessException.class,
                () -> Notice.create(
                        AUTHOR_ID,
                        CATEGORY_ID,
                        tooLongTitle,
                        CONTENT,
                        false,
                        true
                )
        );
    }

    @Test
    @DisplayName("제목이 정확히 255자면 생성할 수 있다")
    void createAllowsTitleAtMaxLength() {
        String maxLengthTitle = "가".repeat(255);

        Notice notice = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                maxLengthTitle,
                CONTENT,
                false,
                true
        );

        assertEquals(255, notice.getTitle().length());
    }

    @Test
    @DisplayName("본문이 비어 있으면 생성할 수 없다")
    void createRejectsBlankContent() {
        assertThrows(
                BusinessException.class,
                () -> Notice.create(
                        AUTHOR_ID,
                        CATEGORY_ID,
                        TITLE,
                        "   ",
                        false,
                        true
                )
        );
    }

    @Test
    @DisplayName("작성자가 없으면 생성할 수 없다")
    void createRejectsNullAuthor() {
        assertThrows(
                BusinessException.class,
                () -> Notice.create(
                        null,
                        CATEGORY_ID,
                        TITLE,
                        CONTENT,
                        false,
                        true
                )
        );
    }

    @Test
    @DisplayName("카테고리가 없으면 생성할 수 없다")
    void createRejectsNullCategory() {
        assertThrows(
                BusinessException.class,
                () -> Notice.create(
                        AUTHOR_ID,
                        null,
                        TITLE,
                        CONTENT,
                        false,
                        true
                )
        );
    }

    @Test
    @DisplayName("훈련생 비공개 공지는 훈련생에게 보이지 않는다")
    void traineeInvisibleNoticeIsHiddenFromTrainee() {
        Notice notice = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                false
        );

        assertFalse(notice.isVisibleTo(ViewerRole.TRAINEE));
        assertTrue(notice.isVisibleTo(ViewerRole.STAFF));
    }

    @Test
    @DisplayName("훈련생 공개 공지는 누구에게나 보인다")
    void traineeVisibleNoticeIsVisibleToEveryone() {
        Notice notice = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                true
        );

        assertTrue(notice.isVisibleTo(ViewerRole.TRAINEE));
        assertTrue(notice.isVisibleTo(ViewerRole.STAFF));
    }

    @Test
    @DisplayName("고정 여부를 그대로 보존한다")
    void keepsPinnedFlag() {
        Notice pinned = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                true,
                true
        );

        Notice normal = Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                true
        );

        /*
         * 고정 여부는 목록 상단 노출에만 쓰인다.
         * 확인 체크박스는 고정 여부와 무관하게 모든 공지에 노출된다.
         */
        assertTrue(pinned.isPinned());
        assertFalse(normal.isPinned());
    }
}
