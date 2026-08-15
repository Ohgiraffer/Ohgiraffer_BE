package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.port.AiCredentialPort;
import com.ohgiraffer.notice.presentation.api.response.AiRewriteKeyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공지 AI 문장 개선", description = "공지 작성 화면의 [AI 문장 개선] 버튼")
@RestController
@RequestMapping("/notices/ai-rewrite-key")
public class NoticeAiRewriteKeyController {

    private final AiCredentialPort aiCredentialPort;

    public NoticeAiRewriteKeyController(AiCredentialPort aiCredentialPort) {
        this.aiCredentialPort = aiCredentialPort;
    }

    @Operation(
            summary = "AI 문장 개선용 제미나이 키 조회",
            description = """
                    공지 작성 화면이 제미나이를 직접 부를 수 있도록 접속 정보를 내려준다.
                    문장을 다듬는 호출 자체는 화면이 구글에 바로 보내며 서버를 거치지 않는다.

                    POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
                    Header: x-goog-api-key: {apiKey}

                    ---
                    이 응답에 담기는 키는 짧은 시간만 쓰는 임시 토큰이 아니라 재사용할 수 있는
                    진짜 키다. 그리고 공지뿐 아니라 평가 요약, 설문 요약, 상담 브리핑,
                    AI 비서, 챗봇이 함께 쓰는 팀 공용 키이며 선불 크레딧으로 동작한다.

                    화면에 내려간 뒤로는 서버가 사용량을 볼 수도 막을 수도 없다. 키가 밖으로
                    새면 크레딧이 소진되어 위 기능들이 한꺼번에 멈추고, 그 호출은 우리 로그에
                    남지 않아 원인을 추적할 수 없다.

                    프론트 요청에 따라 이 방식으로 만들었다. 대안은 문장을 서버로 보내 다듬어
                    돌려받는 것이며, 그 경우 키가 서버 밖으로 나가지 않는다.
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
                    responseCode = "503",
                    description = "서버에 AI 설정이 없음 (AI_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping
    public ResponseEntity<AiRewriteKeyResponse> find() {
        /*
         * 캐시를 막는다. 키가 브라우저 디스크 캐시나 중간 프록시에 남으면
         * 로그아웃한 뒤에도 파일로 남아 있게 된다.
         */
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(AiRewriteKeyResponse.from(aiCredentialPort.get()));
    }
}
