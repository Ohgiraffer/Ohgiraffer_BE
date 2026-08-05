package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.domain.model.Notice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 카테고리 확인과 공지 저장 사이의 경합을 어떻게 알리는지 확인한다.
 *
 * <p>서비스가 저장 전에 카테고리 존재를 확인하지만, 그 사이에 카테고리가 삭제되면
 * 외래키 제약만 남는다. 그때도 500 이 아니라 없는 카테고리와 같은 404 로 나가야 한다.
 */
@ExtendWith(MockitoExtension.class)
class NoticeRepositoryAdapterTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long CATEGORY_ID = 2L;

    @Mock
    private SpringDataNoticeRepository springDataNoticeRepository;

    private NoticeRepositoryAdapter noticeRepositoryAdapter;

    @BeforeEach
    void setUp() {
        noticeRepositoryAdapter =
                new NoticeRepositoryAdapter(springDataNoticeRepository);
    }

    @Test
    @DisplayName("저장 직전 카테고리가 사라지면 없는 카테고리로 알린다")
    void translatesCategoryReferenceViolation() {
        when(springDataNoticeRepository
                .saveAndFlush(any(NoticeJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "could not execute statement",
                        new RuntimeException(
                                "Cannot add or update a child row:"
                                        + " a foreign key constraint fails"
                                        + " (`campflow`.`notice`, CONSTRAINT"
                                        + " `FK_notice_category_TO_notice_1`)"
                        )
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeRepositoryAdapter.save(notice())
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("작성자 외래키 위반은 업무 오류로 바꾸지 않고 그대로 올려보낸다")
    void rethrowsAuthorReferenceViolation() {
        DataIntegrityViolationException thrown =
                new DataIntegrityViolationException(
                        "could not execute statement",
                        new RuntimeException(
                                "Cannot add or update a child row:"
                                        + " a foreign key constraint fails"
                                        + " (`campflow`.`notice`, CONSTRAINT"
                                        + " `FK_users_TO_notice_1`)"
                        )
                );

        when(springDataNoticeRepository
                .saveAndFlush(any(NoticeJpaEntity.class)))
                .thenThrow(thrown);

        /*
         * 인증을 통과한 사용자가 users 에 없다는 뜻이라 화면에 안내할 업무 오류가 아니다.
         */
        DataIntegrityViolationException actual = assertThrows(
                DataIntegrityViolationException.class,
                () -> noticeRepositoryAdapter.save(notice())
        );

        assertSame(thrown, actual);
    }

    @Test
    @DisplayName("정상 저장은 저장된 공지를 돌려준다")
    void savesNotice() {
        when(springDataNoticeRepository
                .saveAndFlush(any(NoticeJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notice saved = noticeRepositoryAdapter.save(notice());

        assertEquals("8월 특강 안내", saved.getTitle());
        assertEquals(CATEGORY_ID, saved.getCategoryId());
    }

    private Notice notice() {
        return Notice.create(
                AUTHOR_ID,
                CATEGORY_ID,
                "8월 특강 안내",
                "<p>본문입니다.</p>",
                false,
                true
        );
    }
}
