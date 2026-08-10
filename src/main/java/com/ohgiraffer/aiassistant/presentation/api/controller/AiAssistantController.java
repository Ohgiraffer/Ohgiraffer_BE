package com.ohgiraffer.aiassistant.presentation.api.controller;

import com.ohgiraffer.aiassistant.application.usecae.BriefingCommandUseCase;
import com.ohgiraffer.aiassistant.application.usecae.BriefingQueryUseCase;
import com.ohgiraffer.aiassistant.presentation.api.response.BriefingResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* comment.
 *  AI 개인비서 브리핑 Controller
 *  - GET: 캐시된 최신 요약 조회 (페이지 진입시)
 *  - POST refresh: 당일 일정+미처리 업무 재분석하여 요약 강제 갱신
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-assistant")
public class AiAssistantController {

    private final BriefingQueryUseCase briefingQueryUseCase;
    private final BriefingCommandUseCase briefingCommandUseCase;

    @GetMapping("/summary")
    public BriefingResponse getSummary(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return BriefingResponse.from(briefingQueryUseCase.getBriefing(principal.getId()));
    }

    @PostMapping("/summary/refresh")
    public BriefingResponse refreshSummary(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return BriefingResponse.from(briefingCommandUseCase.refreshBriefing(principal.getId()));
    }

}
