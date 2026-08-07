package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.port.AuthorNameQueryPort;
import com.ohgiraffer.notice.application.query.NoticeDashboardView;
import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.query.NoticeSummaryView;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeAttachmentRepository;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    private static final Long NOTICE_ID = 10L;
    private static final Long AUTHOR_ID = 1L;
    private static final Long VIEWER_ID = 7L;
    private static final Long CATEGORY_ID = 2L;
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T03:00:00Z");

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeCategoryRepository noticeCategoryRepository;

    @Mock
    private NoticeConfirmationRepository noticeConfirmationRepository;

    @Mock
    private NoticeAttachmentRepository noticeAttachmentRepository;

    @Mock
    private AuthorNameQueryPort authorNameQueryPort;

    private NoticeQueryService noticeQueryService;

    @BeforeEach
    void setUp() {
        noticeQueryService = new NoticeQueryService(
                noticeRepository,
                noticeCategoryRepository,
                noticeConfirmationRepository,
                noticeAttachmentRepository,
                authorNameQueryPort
        );
    }

    @Test
    @DisplayName("공지 상세를 조회하면 카테고리명을 함께 담아 돌려준다")
    void findDetail() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.TRAINEE,
                VIEWER_ID
            );

        assertEquals(NOTICE_ID, view.noticeId());
        assertEquals(CATEGORY_ID, view.categoryId());
        assertEquals("과제", view.categoryName());
        assertEquals("8월 특강 안내", view.title());
        assertEquals(AUTHOR_ID, view.authorId());
        assertTrue(view.pinned());
        assertEquals(CREATED_AT, view.createdAt());
    }

    @Test
    @DisplayName("존재하지 않는 공지면 404로 알리고 카테고리는 조회하지 않는다")
    void findDetailFailsWhenNoticeMissing() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeQueryService.findDetail(
                        NOTICE_ID,
                        ViewerRole.STAFF,
                        VIEWER_ID
                    )
        );

        assertEquals(
                ErrorCode.NOTICE_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(noticeCategoryRepository, never()).findById(CATEGORY_ID);
    }

    @Test
    @DisplayName("훈련생 비공개 공지는 훈련생에게 존재 자체를 알리지 않는다")
    void findDetailHidesTraineeInvisibleNoticeFromTrainee() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(false)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeQueryService.findDetail(
                        NOTICE_ID,
                        ViewerRole.TRAINEE,
                        VIEWER_ID
                    )
        );

        /*
         * 403이면 "그 번호의 공지가 있다"는 사실이 드러나므로 404여야 한다.
         */
        assertEquals(
                ErrorCode.NOTICE_NOT_FOUND,
                exception.getErrorCode()
        );
        assertEquals("존재하지 않는 공지입니다.", exception.getMessage());
        verify(noticeCategoryRepository, never()).findById(CATEGORY_ID);
    }

    @Test
    @DisplayName("훈련생 비공개 공지라도 운영진은 조회할 수 있다")
    void findDetailAllowsStaffToSeeTraineeInvisibleNotice() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(false)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "운영")
                ));

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.STAFF,
                VIEWER_ID
            );

        assertEquals(NOTICE_ID, view.noticeId());
        assertEquals("운영", view.categoryName());
    }

    @Test
    @DisplayName("카테고리를 찾지 못해도 상세 조회는 성공하고 이름만 비운다")
    void findDetailToleratesMissingCategory() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.STAFF,
                VIEWER_ID
            );

        assertEquals(NOTICE_ID, view.noticeId());
        assertNull(view.categoryName());
    }

    @Test
    @DisplayName("목록 조회는 조회자와 카테고리 조건을 그대로 저장소에 넘긴다")
    void findAllDelegatesConditions() {
        when(noticeRepository.findAllVisible(ViewerRole.TRAINEE, 2L))
                .thenReturn(List.of(notice(true)));
        when(noticeCategoryRepository.findAll())
                .thenReturn(List.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.TRAINEE, 2L, VIEWER_ID);

        assertEquals(1, views.size());
        assertEquals(NOTICE_ID, views.get(0).noticeId());
        assertEquals("과제", views.get(0).categoryName());
        assertEquals(AUTHOR_ID, views.get(0).authorId());
        verify(noticeRepository).findAllVisible(ViewerRole.TRAINEE, 2L);
    }

    @Test
    @DisplayName("목록이 비면 카테고리를 조회하지 않고 빈 목록을 돌려준다")
    void findAllSkipsCategoryLookupWhenEmpty() {
        when(noticeRepository.findAllVisible(ViewerRole.STAFF, null))
                .thenReturn(List.of());

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.STAFF, null, VIEWER_ID);

        assertTrue(views.isEmpty());
        verify(noticeCategoryRepository, never()).findAll();
    }

    @Test
    @DisplayName("공지가 여러 건이어도 카테고리는 한 번만 조회한다")
    void findAllLooksUpCategoriesOnce() {
        when(noticeRepository.findAllVisible(ViewerRole.STAFF, null))
                .thenReturn(List.of(notice(true), notice(false), notice(true)));
        when(noticeCategoryRepository.findAll())
                .thenReturn(List.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.STAFF, null, VIEWER_ID);

        assertEquals(3, views.size());
        verify(noticeCategoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("고정 공지 상세는 확인 인원과 내 확인 여부를 함께 담는다")
    void findDetailFillsConfirmation() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(noticeConfirmationRepository.countBy(NOTICE_ID))
                .thenReturn(12L);
        when(noticeConfirmationRepository.existsBy(NOTICE_ID, VIEWER_ID))
                .thenReturn(true);

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.TRAINEE,
                VIEWER_ID
        );

        assertEquals(12L, view.confirmationCount());
        assertTrue(view.confirmedByMe());
    }

    @Test
    @DisplayName("일반 공지 상세에도 확인 정보를 채운다")
    void findDetailFillsConfirmationForNormalNotice() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true, false)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(noticeConfirmationRepository.countBy(NOTICE_ID))
                .thenReturn(5L);
        when(noticeConfirmationRepository.existsBy(NOTICE_ID, VIEWER_ID))
                .thenReturn(true);

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.TRAINEE,
                VIEWER_ID
        );

        /*
         * 확인 체크박스는 고정 여부와 무관하게 모든 공지에 노출된다.
         */
        assertFalse(view.pinned());
        assertEquals(5L, view.confirmationCount());
        assertTrue(view.confirmedByMe());
    }

    @Test
    @DisplayName("목록의 확인 여부는 고정·일반을 가리지 않고 한 번에 조회한다")
    void findAllFetchesConfirmationsInOneQuery() {
        Notice pinned = notice(NOTICE_ID, true, true);
        Notice normal = notice(11L, true, false);

        when(noticeRepository.findAllVisible(ViewerRole.TRAINEE, null))
                .thenReturn(List.of(pinned, normal));
        when(noticeCategoryRepository.findAll())
                .thenReturn(List.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(noticeConfirmationRepository
                .findConfirmedNoticeIds(VIEWER_ID, List.of(NOTICE_ID, 11L)))
                .thenReturn(Set.of(11L));

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.TRAINEE, null, VIEWER_ID);

        /*
         * 확인한 쪽이 일반 공지다. 고정 여부와 확인 여부는 별개다.
         */
        assertFalse(views.get(0).confirmedByMe());
        assertTrue(views.get(1).confirmedByMe());
        verify(noticeConfirmationRepository, times(1))
                .findConfirmedNoticeIds(VIEWER_ID, List.of(NOTICE_ID, 11L));
    }

    @Test
    @DisplayName("고정 공지가 없어도 확인 여부는 조회한다")
    void findAllFetchesConfirmationWithoutPinnedNotice() {
        when(noticeRepository.findAllVisible(ViewerRole.STAFF, null))
                .thenReturn(List.of(notice(11L, true, false)));
        when(noticeCategoryRepository.findAll())
                .thenReturn(List.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(noticeConfirmationRepository
                .findConfirmedNoticeIds(VIEWER_ID, List.of(11L)))
                .thenReturn(Set.of(11L));

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.STAFF, null, VIEWER_ID);

        assertTrue(views.get(0).confirmedByMe());
    }

    @Test
    @DisplayName("상세 조회는 작성자 이름을 함께 담는다")
    void findDetailFillsAuthorName() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(authorNameQueryPort.findName(AUTHOR_ID))
                .thenReturn(Optional.of("박강사"));

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.TRAINEE,
                VIEWER_ID
        );

        assertEquals("박강사", view.authorName());
    }

    @Test
    @DisplayName("작성자를 찾지 못해도 상세 조회는 성공하고 이름만 비운다")
    void findDetailToleratesMissingAuthor() {
        when(noticeRepository.findById(NOTICE_ID))
                .thenReturn(Optional.of(notice(true)));
        when(noticeCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(authorNameQueryPort.findName(AUTHOR_ID))
                .thenReturn(Optional.empty());

        NoticeDetailView view = noticeQueryService.findDetail(
                NOTICE_ID,
                ViewerRole.TRAINEE,
                VIEWER_ID
        );

        assertEquals(NOTICE_ID, view.noticeId());
        assertNull(view.authorName());
    }

    @Test
    @DisplayName("목록의 작성자 이름은 한 번의 조회로 채운다")
    void findAllFetchesAuthorNamesInOneCall() {
        when(noticeRepository.findAllVisible(ViewerRole.STAFF, null))
                .thenReturn(List.of(notice(true), notice(true)));
        when(noticeCategoryRepository.findAll())
                .thenReturn(List.of(
                        NoticeCategory.restore(CATEGORY_ID, "과제")
                ));
        when(authorNameQueryPort.findNames(List.of(AUTHOR_ID, AUTHOR_ID)))
                .thenReturn(Map.of(AUTHOR_ID, "박강사"));

        List<NoticeSummaryView> views =
                noticeQueryService.findAll(ViewerRole.STAFF, null, VIEWER_ID);

        assertEquals("박강사", views.get(0).authorName());
        assertEquals("박강사", views.get(1).authorName());
        verify(authorNameQueryPort, times(1)).findNames(any());
    }

    @Test
    @DisplayName("대시보드 요약은 조회자와 사용자를 그대로 저장소에 넘긴다")
    void findDashboardSummaryDelegatesConditions() {
        when(noticeRepository.findDashboardSummary(
                eq(ViewerRole.TRAINEE), eq(VIEWER_ID), any(Instant.class)))
                .thenReturn(List.of(notice(NOTICE_ID, true, true)));

        List<NoticeDashboardView> cards = noticeQueryService
                .findDashboardSummary(ViewerRole.TRAINEE, VIEWER_ID);

        assertEquals(1, cards.size());
        assertEquals(NOTICE_ID, cards.get(0).noticeId());
        assertEquals("8월 특강 안내", cards.get(0).title());
        assertTrue(cards.get(0).pinned());
    }

    @Test
    @DisplayName("고정 공지 기준 시각은 사흘 전 날짜의 한국 시간 0시다")
    void findDashboardSummaryUsesKstDateBoundary() {
        when(noticeRepository.findDashboardSummary(
                any(), any(), any(Instant.class)))
                .thenReturn(List.of());

        noticeQueryService.findDashboardSummary(ViewerRole.STAFF, VIEWER_ID);

        ArgumentCaptor<Instant> captor =
                ArgumentCaptor.forClass(Instant.class);
        verify(noticeRepository)
                .findDashboardSummary(any(), any(), captor.capture());

        /*
         * 72시간을 빼면 사흘 전 오전에 올라온 공지가 그날 오후에 사라진다.
         * 날짜 0시를 기준으로 삼아 그날 것은 시각과 무관하게 모두 포함한다.
         */
        ZoneId kst = ZoneId.of("Asia/Seoul");
        Instant expected = LocalDate.now(kst)
                .minusDays(3)
                .atStartOfDay(kst)
                .toInstant();

        assertEquals(expected, captor.getValue());
    }

    @Test
    @DisplayName("대시보드에 보여줄 공지가 없으면 빈 목록을 돌려준다")
    void findDashboardSummaryReturnsEmpty() {
        when(noticeRepository.findDashboardSummary(any(), any(), any()))
                .thenReturn(List.of());

        List<NoticeDashboardView> cards = noticeQueryService
                .findDashboardSummary(ViewerRole.STAFF, VIEWER_ID);

        assertTrue(cards.isEmpty());
    }

    private Notice notice(boolean visibleToTrainee) {
        return notice(NOTICE_ID, visibleToTrainee, true);
    }

    private Notice notice(boolean visibleToTrainee, boolean pinned) {
        return notice(NOTICE_ID, visibleToTrainee, pinned);
    }

    private Notice notice(
            Long noticeId,
            boolean visibleToTrainee,
            boolean pinned
    ) {
        return Notice.restore(
                noticeId,
                AUTHOR_ID,
                CATEGORY_ID,
                "8월 특강 안내",
                "<p>본문입니다.</p>",
                pinned,
                visibleToTrainee,
                CREATED_AT,
                CREATED_AT
        );
    }
}
