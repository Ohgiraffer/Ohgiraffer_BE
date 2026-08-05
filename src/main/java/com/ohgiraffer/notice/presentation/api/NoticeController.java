package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.usecase.NoticeCommandUseCase;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.presentation.api.request.CreateNoticeRequest;
import com.ohgiraffer.notice.presentation.api.response.CreateNoticeResponse;
import com.ohgiraffer.notice.presentation.api.response.NoticeDetailResponse;
import com.ohgiraffer.notice.presentation.api.response.NoticeSummaryResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "공지사항", description = "공지 등록과 조회")
@RestController
@RequestMapping("/notices")
public class NoticeController {

    /**
     * 인증 도메인이 훈련생 역할에 부여하는 권한 이름.
     */
    private static final String TRAINEE_AUTHORITY = "ROLE_STUDENT";

    private final NoticeCommandUseCase noticeCommandUseCase;
    private final NoticeQueryUseCase noticeQueryUseCase;

    public NoticeController(
            NoticeCommandUseCase noticeCommandUseCase,
            NoticeQueryUseCase noticeQueryUseCase
    ) {
        this.noticeCommandUseCase = noticeCommandUseCase;
        this.noticeQueryUseCase = noticeQueryUseCase;
    }

    /**
     * 공지 등록. 작성자는 클라이언트가 지정하지 않고 로그인 사용자로 채운다.
     *
     * <p>요구사항상 공지 작성은 운영진(강사·매니저)만 가능하다.
     */
    @Operation(
            summary = "공지 등록",
            description = """
                    운영진(강사·매니저)만 등록할 수 있다.
                    작성자는 요청 값이 아니라 로그인 사용자로 채우므로 본문에 담지 않는다.
                    성공 시 Location 헤더로 생성된 공지의 경로를 함께 내려준다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수 값 누락 또는 형식 오류. errors 에 필드별 사유가 담긴다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 카테고리 (NOTICE_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<CreateNoticeResponse> create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateNoticeRequest request
    ) {
        Notice notice = noticeCommandUseCase.create(
                request.toCommand(currentUserId(principal))
        );

        return ResponseEntity
                .created(URI.create("/notices/" + notice.getId()))
                .body(CreateNoticeResponse.from(notice));
    }

    /**
     * 공지 목록 조회. 훈련생에게는 훈련생 비공개 공지를 제외하고 반환한다.
     */
    @Operation(
            summary = "공지 목록 조회",
            description = """
                    필수 공지를 상단에 두고 같은 등급 안에서는 최신순으로 정렬해 전체를 반환한다.
                    페이지네이션은 화면에서 처리하기로 해 서버는 자르지 않는다.
                    훈련생에게는 훈련생 비공개 공지를 제외한다.
                    작성자 이름과 확인 여부는 아직 내려주지 않는다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공. 비어 있을 수 있다")
    @GetMapping
    public ResponseEntity<List<NoticeSummaryResponse>> findAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "카테고리 탭 필터. 생략하면 전체", example = "1")
            @RequestParam(required = false) Long categoryId
    ) {
        List<NoticeSummaryResponse> notices =
                noticeQueryUseCase.findAll(viewerRole(principal), categoryId)
                        .stream()
                        .map(NoticeSummaryResponse::from)
                        .toList();

        return ResponseEntity.ok(notices);
    }

    /**
     * 공지 상세 조회. 훈련생 비공개 공지는 훈련생에게 노출하지 않는다.
     */
    @Operation(
            summary = "공지 상세 조회",
            description = """
                    훈련생 비공개 공지는 훈련생에게 404 로 응답한다.
                    403 을 주면 해당 번호의 공지가 존재한다는 사실이 드러나기 때문이다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 조회 권한이 없는 공지 (NOTICE_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> findDetail(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "공지 식별자", example = "1")
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(
                NoticeDetailResponse.from(
                        noticeQueryUseCase.findDetail(
                                noticeId,
                                viewerRole(principal)
                        )
                )
        );
    }

    /**
     * SecurityConfig 가 /notices 전체에 인증을 요구하므로 정상 흐름에서는 null 이 아니다.
     * 설정이 바뀌어 인증 없이 도달했을 때 NullPointerException 대신 401 로 알리기 위한 방어다.
     */
    private Long currentUserId(CustomUserPrincipal principal) {
        requireAuthenticated(principal);

        return principal.id();
    }

    /**
     * 인증 도메인의 역할을 공지 도메인이 쓰는 조회자 구분으로 좁힌다.
     */
    private ViewerRole viewerRole(CustomUserPrincipal principal) {
        requireAuthenticated(principal);

        boolean trainee = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(TRAINEE_AUTHORITY::equals);

        return trainee ? ViewerRole.TRAINEE : ViewerRole.STAFF;
    }

    private void requireAuthenticated(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
