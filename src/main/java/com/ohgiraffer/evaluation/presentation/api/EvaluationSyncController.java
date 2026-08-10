package com.ohgiraffer.evaluation.presentation.api;

import com.ohgiraffer.evaluation.application.usecase.EvaluationSyncUseCase;
import com.ohgiraffer.evaluation.presentation.api.response.EvaluationSyncResponse;
import com.ohgiraffer.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "평가 시트 동기화", description = "평가 관리 화면의 동기화 실행 탭")
@RestController
@RequestMapping("/evaluations/sync")
public class EvaluationSyncController {

    private final EvaluationSyncUseCase evaluationSyncUseCase;

    public EvaluationSyncController(
            EvaluationSyncUseCase evaluationSyncUseCase
    ) {
        this.evaluationSyncUseCase = evaluationSyncUseCase;
    }

    @Operation(
            summary = "평가 시트 동기화 실행",
            description = """
                    화면의 [동기화 실행] 버튼이 호출한다. 저장된 연동 설정으로 시트를 읽어
                    평가 데이터를 최신화한다.

                    같은 평가를 두 번 저장하지 않도록 훈련생·평가유형·평가항목을 이어 붙인
                    식별값으로 대조한다. 점수나 의견만 바뀌면 그 값을 갱신하고,
                    처음 보는 식별값이면 새로 저장한다.

                    한 행이 잘못돼도 나머지는 반영한다. 시트가 100행인데 이메일 오타 하나로
                    아무것도 들어가지 않으면 쓰기 어렵기 때문이다. 반영하지 못한 행은
                    skipped 에 줄 번호와 이유가 담긴다.

                    구글 시트는 한 번만 읽는다. 이전 상태는 저장된 평가에서 가져와 비교한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 완료"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            저장해 둔 컬럼이 시트에 없음 (EVALUATION_002).
                            시트에서 컬럼 이름을 바꾸면 발생한다. 연동 설정을 다시 저장해야 한다
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            훈련생이 호출 (AUTH_002)
                            또는 서비스 계정에 시트가 공유되지 않음 (SHEET_002)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "연동된 시트가 없음 (EVALUATION_001). 연동 설정을 먼저 저장해야 한다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "구글 시트 호출 한도 초과 (SHEET_004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping
    public ResponseEntity<EvaluationSyncResponse> sync() {
        return ResponseEntity.ok(
                EvaluationSyncResponse.from(evaluationSyncUseCase.sync())
        );
    }
}
