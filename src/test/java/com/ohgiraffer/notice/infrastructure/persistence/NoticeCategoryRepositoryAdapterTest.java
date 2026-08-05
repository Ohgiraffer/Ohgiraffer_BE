package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * 검사와 실행 사이의 경합을 어떻게 알리는지 확인한다.
 *
 * <p>서비스가 미리 이름 중복과 사용 여부를 보지만, 그 사이에 다른 요청이 끼어들면
 * DB 제약만 남는다. 그때도 500 이 아니라 평상시와 같은 409 로 나가야 한다.
 * <ul>
 *   <li>등록 — 같은 이름이 먼저 들어온 경우 (UNIQUE)</li>
 *   <li>삭제 — 세고 난 뒤 그 카테고리로 공지가 등록된 경우 (외래키)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NoticeCategoryRepositoryAdapterTest {

    private static final Long CATEGORY_ID = 5L;

    @Mock
    private SpringDataNoticeCategoryRepository springDataNoticeCategoryRepository;

    private NoticeCategoryRepositoryAdapter noticeCategoryRepositoryAdapter;

    @BeforeEach
    void setUp() {
        noticeCategoryRepositoryAdapter = new NoticeCategoryRepositoryAdapter(
                springDataNoticeCategoryRepository
        );
    }

    @Test
    @DisplayName("이름 UNIQUE 제약을 어기면 중복 오류로 바꿔 알린다")
    void translatesUniqueNameViolation() {
        when(springDataNoticeCategoryRepository
                .saveAndFlush(any(NoticeCategoryJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "could not execute statement",
                        new RuntimeException(
                                "Duplicate entry '기타' for key"
                                        + " 'UQ_NOTICE_CATEGORY_NAME'"
                        )
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCategoryRepositoryAdapter.save(
                        NoticeCategory.create("기타"))
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_DUPLICATE_NAME,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("이름 제약이 아닌 무결성 오류는 그대로 올려보낸다")
    void rethrowsOtherIntegrityViolations() {
        DataIntegrityViolationException thrown =
                new DataIntegrityViolationException(
                        "Column 'name' cannot be null"
                );

        when(springDataNoticeCategoryRepository
                .saveAndFlush(any(NoticeCategoryJpaEntity.class)))
                .thenThrow(thrown);

        DataIntegrityViolationException actual = assertThrows(
                DataIntegrityViolationException.class,
                () -> noticeCategoryRepositoryAdapter.save(
                        NoticeCategory.create("기타"))
        );

        assertSame(thrown, actual);
    }

    @Test
    @DisplayName("공지가 참조 중이면 외래키 위반을 사용 중 오류로 바꿔 알린다")
    void translatesNoticeReferenceViolation() {
        doThrow(new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException(
                        "Cannot delete or update a parent row:"
                                + " a foreign key constraint fails"
                                + " (`campflow`.`notice`, CONSTRAINT"
                                + " `FK_notice_category_TO_notice_1`)"
                )
        )).when(springDataNoticeCategoryRepository).flush();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeCategoryRepositoryAdapter.deleteById(CATEGORY_ID)
        );

        assertEquals(
                ErrorCode.NOTICE_CATEGORY_IN_USE,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("삭제에서도 다른 무결성 오류는 그대로 올려보낸다")
    void rethrowsOtherViolationsOnDelete() {
        DataIntegrityViolationException thrown =
                new DataIntegrityViolationException("다른 제약 위반");

        doThrow(thrown).when(springDataNoticeCategoryRepository).flush();

        DataIntegrityViolationException actual = assertThrows(
                DataIntegrityViolationException.class,
                () -> noticeCategoryRepositoryAdapter.deleteById(CATEGORY_ID)
        );

        assertSame(thrown, actual);
    }

    @Test
    @DisplayName("정상 삭제는 DELETE 를 즉시 내보내 제약 위반을 잡을 수 있게 한다")
    void deleteFlushesImmediately() {
        noticeCategoryRepositoryAdapter.deleteById(CATEGORY_ID);

        InOrder inOrder = inOrder(springDataNoticeCategoryRepository);
        inOrder.verify(springDataNoticeCategoryRepository)
                .deleteById(CATEGORY_ID);
        inOrder.verify(springDataNoticeCategoryRepository).flush();
    }

    @Test
    @DisplayName("정상 저장은 채번된 식별자를 담아 돌려준다")
    void savesCategory() {
        when(springDataNoticeCategoryRepository
                .saveAndFlush(any(NoticeCategoryJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoticeCategory saved = noticeCategoryRepositoryAdapter.save(
                NoticeCategory.create("기타")
        );

        assertEquals("기타", saved.getName());
    }
}
