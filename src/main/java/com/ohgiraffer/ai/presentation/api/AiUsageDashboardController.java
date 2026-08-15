package com.ohgiraffer.ai.presentation.api;

import com.ohgiraffer.ai.application.service.AiUsageDashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI 사용량", description = "AI(Gemini) 사용량 대시보드 화면")
@Controller
@RequestMapping("/admin/ai-usage")
@RequiredArgsConstructor
public class AiUsageDashboardController {

    private final AiUsageDashboardQueryService aiUsageDashboardQueryService;

    @Operation(summary = "오늘 AI 사용량 대시보드 조회", description = "오늘 기준 기능별 호출 현황, 실패 원인 분포, 시간대별 호출 추이를 Thymeleaf 화면으로 렌더링합니다.")
    @GetMapping("/dashboard")
    //http://localhost:8080/admin/ai-usage/dashboard
    public String dashboard(Model model) {
        var result = aiUsageDashboardQueryService.getTodayDashboard();

        model.addAttribute("byFeature", result.byFeature());
        model.addAttribute("failReasons", result.failReasons());
        model.addAttribute("hourly", result.hourly());
        model.addAttribute("totalCalls", result.totalCalls());
        model.addAttribute("failCount", result.failCount());
        model.addAttribute("today", result.today());

        return "ai-usage/dashboard";
    }
}