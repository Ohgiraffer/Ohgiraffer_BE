package com.ohgiraffer.evaluation.presentation.api;

import com.ohgiraffer.evaluation.application.usecase.EvaluationSheetLinkUseCase;
import com.ohgiraffer.evaluation.presentation.api.request.SaveEvaluationSheetLinkRequest;
import com.ohgiraffer.evaluation.presentation.api.response.EvaluationSheetLinkResponse;
import com.ohgiraffer.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "평가 시트 연동", description = "평가 관리 화면의 연동 설정 탭")
@RestController
@RequestMapping("/evaluations/sheet-link")
public class EvaluationSheetLinkController {

    private final EvaluationSheetLinkUseCase evaluationSheetLinkUseCase;

    public EvaluationSheetLinkController(
            EvaluationSheetLinkUseCase evaluationSheetLinkUseCase
    ) {
        this.evaluationSheetLinkUseCase = evaluationSheetLinkUseCase;
    }

    @Operation(
            summary = "평가 시트 연동 설정 조회",
            description = """
                    연동 설정 탭에 들어올 때 호출한다. 화면을 어느 상태로 그릴지 이 응답이 정한다.

                    아직 연동한 적이 없으면 204 로 답한다. 주소 입력칸을 비운 채로 시작하면 된다.
                    200 이면 저장된 주소와 컬럼 매핑을 채우고 '연결됨' 으로 표시한다.

                    구글 시트를 호출하지 않는다. 저장해 둔 값만 돌려주므로 호출 한도와 무관하다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장된 연동 설정"),
            @ApiResponse(responseCode = "204", description = "아직 연동한 적 없음"),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping
    public ResponseEntity<EvaluationSheetLinkResponse> find() {
        return evaluationSheetLinkUseCase.find()
                .map(EvaluationSheetLinkResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "평가 시트 연동 설정 저장",
            description = """
                    화면의 [설정 저장] 버튼이 호출한다. 이미 연동돼 있으면 덮어쓴다.
                    평가 연동은 한 줄뿐이라 [수정] 도 같은 경로를 쓴다.

                    저장 전에 시트를 실제로 열어 세 가지를 확인한다.
                    1. 주소가 올바르고 서비스 계정이 읽을 수 있는가
                    2. 지정한 탭이 그 스프레드시트에 있는가
                    3. 짝지은 컬럼이 그 탭에 실제로 있는가

                    통과시켜 두면 나중에 동기화가 실패하는데, 그때는 무엇이 잘못됐는지
                    사용자가 알기 어렵다.

                    컬럼 이름은 연결 확인(POST /external-sheets/validate) 응답의
                    columns 에서 고른 값을 그대로 보내면 된다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            필수 값 누락 (COMMON_001), 주소 형식 오류 (SHEET_001),
                            시트에 없는 컬럼 (EVALUATION_002), 시트에 없는 탭 (EVALUATION_003)
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
                    responseCode = "502",
                    description = "구글 시트 호출 실패 (SHEET_005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping
    public ResponseEntity<EvaluationSheetLinkResponse> save(
            @Valid @RequestBody SaveEvaluationSheetLinkRequest request
    ) {
        return ResponseEntity.ok(
                EvaluationSheetLinkResponse.from(
                        evaluationSheetLinkUseCase.save(request.toCommand())
                )
        );
    }
}
