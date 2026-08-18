package com.ohgiraffer.ai.presentation.api;

import com.ohgiraffer.ai.application.service.AiUsageStatusQueryService;
import com.ohgiraffer.ai.presentation.api.response.AiStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 사용량", description = "AI(Gemini) 호출 상태 즉시 조회 API")
@RestController
@RequestMapping("/admin/ai-usage/status")
@RequiredArgsConstructor
public class AiUsageController {

    private final AiUsageStatusQueryService aiUsageStatusQueryService;

    @Operation(summary = "오늘 AI 호출 상태 조회", description = "오늘 기준 총 호출 수, 실패 수, 마지막 호출 결과와 실패 원인(429/키오류/코드오류)을 진단 메시지와 함께 반환합니다.")
    @GetMapping
    // http://localhost:8080/admin/ai-usage/status
    public ResponseEntity<AiStatusResponse> getStatus() {
        var result = aiUsageStatusQueryService.getTodayStatus();

        return ResponseEntity.ok(new AiStatusResponse(
                result.totalCallsToday(),
                result.failCallsToday(),
                result.lastCallAt(),
                result.lastCallSuccess(),
                result.lastFailReason(),
                result.diagnosis()
        ));
    }
}