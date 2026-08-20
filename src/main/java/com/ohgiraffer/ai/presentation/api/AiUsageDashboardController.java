package com.ohgiraffer.ai.presentation.api;

import com.ohgiraffer.ai.application.service.AiUsageDashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "AI 사용량", description = "AI(Gemini) 사용량 대시보드 화면")
@Controller
@RequestMapping("/admin/ai-usage")
@RequiredArgsConstructor
public class AiUsageDashboardController {

    private final AiUsageDashboardQueryService aiUsageDashboardQueryService;

    @Operation(summary = "AI 사용량 대시보드 조회", description = "선택한 날짜(기본값: 오늘) 기준 기능별 호출 현황, 실패 원인 분포, 시간대별 호출 추이를 Thymeleaf 화면으로 렌더링합니다.")
    @GetMapping("/dashboard")
    //http://localhost:8080/admin/ai-usage/dashboard?date=2026-08-15
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        var result = aiUsageDashboardQueryService.getDashboard(targetDate);

        model.addAttribute("byFeature", result.byFeature());
        model.addAttribute("failReasons", result.failReasons());
        model.addAttribute("hourly", result.hourly());
        model.addAttribute("totalCalls", result.totalCalls());
        model.addAttribute("failCount", result.failCount());
        model.addAttribute("today", result.today());
        model.addAttribute("selectedDate", targetDate);
        model.addAttribute("isToday", targetDate.isEqual(LocalDate.now()));

        return "ai-usage/dashboard";
    }
}