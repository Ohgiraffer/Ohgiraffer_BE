package com.ohgiraffer.aiops.presentation.api.controller;

import com.ohgiraffer.aiops.application.usecase.SaveAgentReasoningLogUseCase;
import com.ohgiraffer.aiops.presentation.api.request.SaveAgentReasoningLogRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * aiops-relay(Node.js) 전용 내부 엔드포인트.
 * 외부 노출 없이 서버 간 통신으로만 호출되며, ADMIN_INTERNAL_TOKEN으로 인증한다.
 * (기존 화이트리스트 액션 실행용 admin 엔드포인트와 같은 토큰/헤더 검증 방식을
 *  이미 별도 필터/인터셉터로 처리하고 있다면 이 컨트롤러의 isValidInternalToken()은
 *  제거하고 그 공통 메커니즘에 편입시키는 게 중복이 없어서 더 낫다.)
 *
 * NOTE: 프로젝트에 공용 ErrorCode(예: UNAUTHORIZED)가 이미 있다면
 * 아래 401 직접 반환 대신 BusinessException + 해당 ErrorCode로 바꿔주는 게
 * 다른 컨트롤러들과 일관성 있음. TEAM_ACCESS_DENIED처럼 다른 도메인 전용
 * 코드를 여기서 재사용하는 건 의미가 안 맞아서 피했음.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/agent-reasoning-logs")
public class AgentReasoningLogController {

    private final SaveAgentReasoningLogUseCase saveAgentReasoningLogUseCase;

    @Value("${admin.internal-token}")
    private String adminInternalToken;

    @PostMapping
    public ResponseEntity<Void> saveReasoningLog(
            @RequestHeader("X-Internal-Token") String internalToken,
            @Valid @RequestBody SaveAgentReasoningLogRequest request
    ) {
        if (!isValidInternalToken(internalToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        saveAgentReasoningLogUseCase.saveReasoningLog(
                request.toCommand()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    private boolean isValidInternalToken(
            String internalToken
    ) {
        return internalToken != null
                && internalToken.equals(adminInternalToken);
    }

}
