package com.ohgiraffer.evaluation.presentation.api;

import com.ohgiraffer.evaluation.application.usecase.EvaluationSyncNotifyUseCase;
import com.ohgiraffer.evaluation.application.usecase.SheetSyncLogQueryUseCase;
import com.ohgiraffer.evaluation.presentation.api.response.EvaluationSyncNotifyResponse;
import com.ohgiraffer.evaluation.presentation.api.response.SheetSyncLogResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "평가 시트 동기화 이력", description = "평가 관리 화면의 이력 탭")
@RestController
@RequestMapping("/evaluations/sync-logs")
public class SheetSyncLogController {

    private final SheetSyncLogQueryUseCase sheetSyncLogQueryUseCase;
    private final EvaluationSyncNotifyUseCase evaluationSyncNotifyUseCase;

    public SheetSyncLogController(
            SheetSyncLogQueryUseCase sheetSyncLogQueryUseCase,
            EvaluationSyncNotifyUseCase evaluationSyncNotifyUseCase
    ) {
        this.sheetSyncLogQueryUseCase = sheetSyncLogQueryUseCase;
        this.evaluationSyncNotifyUseCase = evaluationSyncNotifyUseCase;
    }

    @Operation(
            summary = "동기화 이력 목록",
            description = """
                    최신순으로 반환한다. 연동한 적이 없으면 빈 배열이다.

                    변경이 있었던 실행만 남는다. 버튼을 눌렀지만 시트에 바뀐 것이 없으면
                    이력을 만들지 않는다. 목록이 0건짜리 행으로 채워지면 정작 볼 것이 묻힌다.

                    구글 시트를 호출하지 않는다. 저장해 둔 기록만 읽는다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공. 비어 있을 수 있다"),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<SheetSyncLogResponse>> findAll() {
        return ResponseEntity.ok(
                sheetSyncLogQueryUseCase.findAll()
                        .stream()
                        .map(SheetSyncLogResponse::from)
                        .toList()
        );
    }

    @Operation(
            summary = "동기화 이력 상세",
            description = """
                    그때 저장해 둔 요약을 그대로 돌려준다. 목록과 같은 형식이다.

                    시트를 다시 읽지 않는다. 지금 시트를 읽으면 그때와 다른 내용이 나오고,
                    이력은 그 시점의 기록이어야 한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 이력 (EVALUATION_004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/{syncLogId}")
    public ResponseEntity<SheetSyncLogResponse> findDetail(
            @Parameter(description = "이력 식별자", example = "1")
            @PathVariable Long syncLogId
    ) {
        return ResponseEntity.ok(
                SheetSyncLogResponse.from(
                        sheetSyncLogQueryUseCase.findDetail(syncLogId))
        );
    }

    @Operation(
            summary = "동기화 결과 알림 보내기",
            description = """
                    이 이력의 요약을 다른 운영진에게 알림으로 보낸다.

                    동기화할 때 자동으로 보내지 않는다. 요구사항이 "버튼을 눌러" 라고 정해서,
                    사람이 요약을 읽고 알릴 만한 내용인지 판단한 뒤 보낸다.

                    받는 사람은 재원 중인 강사와 매니저다. 평가 관리 화면을 운영진만 볼 수 있어
                    훈련생에게는 보내지 않는다. 보낸 사람 자신도 뺀다.
                    방금 화면에서 요약을 본 사람에게 같은 내용을 또 보낼 이유가 없다.

                    여러 번 눌러도 막지 않는다. 같은 내용이 다시 갈 뿐이라,
                    화면에서 보낸 뒤 버튼을 잠가 주면 된다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발송 완료"),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 이력 (EVALUATION_004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping("/{syncLogId}/notify")
    public ResponseEntity<EvaluationSyncNotifyResponse> notifyStaff(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "이력 식별자", example = "1")
            @PathVariable Long syncLogId
    ) {
        return ResponseEntity.ok(
                new EvaluationSyncNotifyResponse(
                        evaluationSyncNotifyUseCase.notify(
                                syncLogId, currentUserId(principal))
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
