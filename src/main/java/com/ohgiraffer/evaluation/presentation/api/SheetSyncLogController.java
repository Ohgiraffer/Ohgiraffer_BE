package com.ohgiraffer.evaluation.presentation.api;

import com.ohgiraffer.evaluation.application.usecase.SheetSyncLogQueryUseCase;
import com.ohgiraffer.evaluation.presentation.api.response.SheetSyncLogResponse;
import com.ohgiraffer.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "평가 시트 동기화 이력", description = "평가 관리 화면의 이력 탭")
@RestController
@RequestMapping("/evaluations/sync-logs")
public class SheetSyncLogController {

    private final SheetSyncLogQueryUseCase sheetSyncLogQueryUseCase;

    public SheetSyncLogController(
            SheetSyncLogQueryUseCase sheetSyncLogQueryUseCase
    ) {
        this.sheetSyncLogQueryUseCase = sheetSyncLogQueryUseCase;
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
}
