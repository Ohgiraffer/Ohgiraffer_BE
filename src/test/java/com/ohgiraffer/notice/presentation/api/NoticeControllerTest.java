package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.query.NoticeConfirmationView;
import com.ohgiraffer.notice.application.query.NoticeDashboardView;
import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.query.NoticeSummaryView;
import com.ohgiraffer.notice.application.usecase.NoticeCommandUseCase;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.presentation.api.request.CreateNoticeRequest;
import com.ohgiraffer.notice.presentation.api.response.NoticeConfirmationResponse;
import com.ohgiraffer.notice.presentation.api.response.NoticeDashboardResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 로그인 사용자가 공지 작성자로 이어지는지 확인한다.
 *
 * <p>인증 도메인에 로그인 API 가 아직 없어 실제 토큰으로는 검증할 수 없으므로,
 * 컨트롤러를 직접 호출해 인증 주체가 작성자로 전달되는지만 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

    private static final Long LOGIN_USER_ID = 7L;
    private static final Long NOTICE_ID = 10L;
    private static final Long CATEGORY_ID = 1L;
    private static final String TITLE = "8월 특강 안내";
    private static final String CONTENT = "<p>본문입니다.</p>";

    @Mock
    private NoticeCommandUseCase noticeCommandUseCase;

    @Mock
    private NoticeQueryUseCase noticeQueryUseCase;

    @Mock
    private S3UrlResolver s3UrlResolver;

    private NoticeController noticeController;

    @BeforeEach
    void setUp() {
        noticeController = new NoticeController(
                noticeCommandUseCase,
                noticeQueryUseCase,
                s3UrlResolver
        );
    }

    @Test
    @DisplayName("공지 작성자는 요청 값이 아니라 로그인 사용자로 채운다")
    void createUsesLoginUserAsAuthor() {
        when(noticeCommandUseCase.create(any(CreateNoticeCommand.class)))
                .thenReturn(savedNotice());

        noticeController.create(principal(), request());

        ArgumentCaptor<CreateNoticeCommand> captor =
                ArgumentCaptor.forClass(CreateNoticeCommand.class);
        verify(noticeCommandUseCase).create(captor.capture());

        assertEquals(LOGIN_USER_ID, captor.getValue().authorId());
        assertEquals(CATEGORY_ID, captor.getValue().categoryId());
        assertEquals(TITLE, captor.getValue().title());
    }

    @Test
    @DisplayName("인증 주체가 없으면 공지를 만들지 않고 401로 알린다")
    void createRejectsMissingPrincipal() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeController.create(null, request())
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(noticeCommandUseCase, never())
                .create(any(CreateNoticeCommand.class));
    }

    @Test
    @DisplayName("훈련생이 조회하면 훈련생 조회자로 유스케이스에 전달한다")
    void findDetailPassesTraineeViewer() {
        when(noticeQueryUseCase.findDetail(any(), any(), any()))
                .thenReturn(detailView());

        noticeController.findDetail(principal(Role.STUDENT), NOTICE_ID);

        verify(noticeQueryUseCase).findDetail(NOTICE_ID, ViewerRole.TRAINEE, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("강사가 조회하면 운영진 조회자로 유스케이스에 전달한다")
    void findDetailPassesStaffViewerForInstructor() {
        when(noticeQueryUseCase.findDetail(any(), any(), any()))
                .thenReturn(detailView());

        noticeController.findDetail(principal(Role.INSTRUCTOR), NOTICE_ID);

        verify(noticeQueryUseCase).findDetail(NOTICE_ID, ViewerRole.STAFF, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("매니저가 조회하면 운영진 조회자로 유스케이스에 전달한다")
    void findDetailPassesStaffViewerForManager() {
        when(noticeQueryUseCase.findDetail(any(), any(), any()))
                .thenReturn(detailView());

        noticeController.findDetail(principal(Role.MANAGER), NOTICE_ID);

        verify(noticeQueryUseCase).findDetail(NOTICE_ID, ViewerRole.STAFF, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("목록 조회는 조회자 구분과 카테고리 필터를 함께 넘긴다")
    void findAllPassesViewerAndCategory() {
        when(noticeQueryUseCase.findAll(any(), any(), any()))
                .thenReturn(List.of(summaryView()));

        noticeController.findAll(principal(Role.STUDENT), 3L);

        verify(noticeQueryUseCase).findAll(ViewerRole.TRAINEE, 3L, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("카테고리를 지정하지 않으면 필터 없이 조회한다")
    void findAllWithoutCategory() {
        when(noticeQueryUseCase.findAll(any(), any(), any()))
                .thenReturn(List.of());

        noticeController.findAll(principal(Role.MANAGER), null);

        verify(noticeQueryUseCase).findAll(ViewerRole.STAFF, null, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("확인 처리는 로그인 사용자로 호출하고 갱신된 인원수를 응답한다")
    void confirmReturnsUpdatedCount() {
        when(noticeCommandUseCase.confirm(NOTICE_ID, ViewerRole.TRAINEE, LOGIN_USER_ID))
                .thenReturn(new NoticeConfirmationView(NOTICE_ID, 12L, true));

        ResponseEntity<NoticeConfirmationResponse> response =
                noticeController.confirm(principal(Role.STUDENT), NOTICE_ID);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(12L, response.getBody().confirmationCount());
        assertTrue(response.getBody().confirmedByMe());
        verify(noticeCommandUseCase)
                .confirm(NOTICE_ID, ViewerRole.TRAINEE, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("대시보드 요약은 조회자 구분과 로그인 사용자를 함께 넘긴다")
    void findDashboardSummaryPassesViewerAndUser() {
        when(noticeQueryUseCase.findDashboardSummary(any(), any()))
                .thenReturn(List.of(new NoticeDashboardView(
                        NOTICE_ID,
                        TITLE,
                        true,
                        Instant.parse("2026-08-04T03:00:00Z")
                )));

        ResponseEntity<List<NoticeDashboardResponse>> response =
                noticeController.findDashboardSummary(principal(Role.STUDENT));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(TITLE, response.getBody().get(0).title());
        assertTrue(response.getBody().get(0).pinned());
        verify(noticeQueryUseCase)
                .findDashboardSummary(ViewerRole.TRAINEE, LOGIN_USER_ID);
    }

    private NoticeSummaryView summaryView() {
        return new NoticeSummaryView(
                NOTICE_ID,
                CATEGORY_ID,
                "수업",
                TITLE,
                LOGIN_USER_ID,
                "이강사",
                false,
                false,
                Instant.parse("2026-08-04T03:00:00Z")
        );
    }

    private CustomUserPrincipal principal() {
        return principal(Role.INSTRUCTOR);
    }

    private CustomUserPrincipal principal(Role role) {
        return CustomUserPrincipal.from(new User(
                LOGIN_USER_ID,
                "이강사",
                "010-0000-0000",
                "instructor@campflow.dev",
                role,
                null,
                "ENCODED_PASSWORD",
                false,
                true,
                LocalDate.of(2026, 8, 1),
                null,
                UserStatus.ACTIVE,
                null
        ));
    }

    private CreateNoticeRequest request() {
        return new CreateNoticeRequest(
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                true,
                List.of()
        );
    }

    private NoticeDetailView detailView() {
        Instant now = Instant.parse("2026-08-04T03:00:00Z");

        return new NoticeDetailView(
                NOTICE_ID,
                CATEGORY_ID,
                "수업",
                TITLE,
                CONTENT,
                LOGIN_USER_ID,
                "이강사",
                false,
                true,
                0L,
                false,
                List.of(),
                now,
                now
        );
    }

    private Notice savedNotice() {
        Instant now = Instant.parse("2026-08-04T03:00:00Z");

        return Notice.restore(
                100L,
                LOGIN_USER_ID,
                CATEGORY_ID,
                TITLE,
                CONTENT,
                false,
                true,
                now,
                now
        );
    }
}
