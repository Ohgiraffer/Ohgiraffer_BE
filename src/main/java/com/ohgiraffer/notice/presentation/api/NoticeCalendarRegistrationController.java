package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.usecase.NoticeCalendarRegistrationUseCase;
import com.ohgiraffer.notice.presentation.api.request.RegisterNoticeSchedulesRequest;
import com.ohgiraffer.notice.presentation.api.response.NoticeCalendarRegistrationResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공지 AI 일정 등록", description = "AI 추출 일정 확인 모달")
@RestController
@RequestMapping("/notices/{noticeId}/calendar-events")
public class NoticeCalendarRegistrationController {

    private final NoticeCalendarRegistrationUseCase noticeCalendarRegistrationUseCase;

    public NoticeCalendarRegistrationController(
            NoticeCalendarRegistrationUseCase noticeCalendarRegistrationUseCase
    ) {
        this.noticeCalendarRegistrationUseCase = noticeCalendarRegistrationUseCase;
    }

    @Operation(
            summary = "확정한 일정을 캘린더에 등록",
            description = """
                    모달의 [선택 일정 캘린더에 등록] 버튼이 호출한다.
                    [이 일정 포함] 을 켠 것만 담아 보내면 된다.

                    추출 응답을 그대로 되돌려 보내지 않는다. 사용자가 값을 고칠 수 있고
                    유형도 이 화면에서 정해지므로, 화면이 들고 있는 값이 맞다.

                    등록하고 나면 이 공지의 aiCalendarRegistered 가 true 가 되어
                    공지 상세에서 AI 일정 등록 컴포넌트가 더 이상 나타나지 않는다.
                    본문을 나중에 수정해도 이 값은 되돌아가지 않는다.

                    등록된 일정은 공지와 끊어져 있다. 공지를 수정하거나 삭제해도
                    캘린더 일정은 그대로 남는다. 모달 상단 안내 문구가 그 이야기다.

                    유형은 필수다. 화면에서 고르지 않은 후보는 보내지 마라.
                    개인 일정과 공휴일은 이 경로로 등록할 수 없다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            필수 값 누락 또는 등록할 수 없는 유형 (COMMON_001, INVALID_INPUT_VALUE)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지 (NOTICE_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            이미 이 공지로 등록함 (NOTICE_013).
                            화면을 열어 둔 사이 다른 운영진이 먼저 등록한 경우다
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping
    public ResponseEntity<NoticeCalendarRegistrationResponse> register(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "공지 식별자", example = "1")
            @PathVariable Long noticeId,
            @Valid @RequestBody RegisterNoticeSchedulesRequest request
    ) {
        return ResponseEntity.ok(
                new NoticeCalendarRegistrationResponse(
                        noticeCalendarRegistrationUseCase.register(
                                noticeId,
                                request.toCommands(),
                                currentUserId(principal)
                        )
                )
        );
    }

    /**
     * SecurityConfig 가 인증을 요구하므로 정상 흐름에서는 null 이 아니다.
     * 설정이 바뀌어 인증 없이 도달했을 때 NullPointerException 대신 401 로 알리기 위한 방어다.
     */
    private Long currentUserId(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return principal.getId();
    }
}
