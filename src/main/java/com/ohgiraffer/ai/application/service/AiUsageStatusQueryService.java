package com.ohgiraffer.ai.application.service;

import com.ohgiraffer.ai.domain.dto.AiUsageLastCall;
import com.ohgiraffer.ai.domain.dto.AiUsageStatusResult;
import com.ohgiraffer.ai.domain.repository.AiUsageLogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiUsageStatusQueryService {

    private final AiUsageLogQueryRepository aiUsageLogQueryRepository;

    public AiUsageStatusResult getTodayStatus() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long totalCallsToday = aiUsageLogQueryRepository.countCallsBetween(todayStart, now);
        long failCallsToday = aiUsageLogQueryRepository.countFailuresBetween(todayStart, now);
        AiUsageLastCall lastCall = aiUsageLogQueryRepository.findLastCall().orElse(null);

        return new AiUsageStatusResult(
                totalCallsToday,
                failCallsToday,
                lastCall != null ? lastCall.createdAt() : null,
                lastCall != null ? lastCall.success() : null,
                lastCall != null && lastCall.failReason() != null ? lastCall.failReason().name() : null,
                diagnose(lastCall, failCallsToday)
        );
    }

    private String diagnose(AiUsageLastCall lastCall, long failCallsToday) {
        if (lastCall == null) {
            return "오늘 호출 기록 없음 -> GeminiClient가 아예 호출되지 않았을 가능성";
        }
        if (!lastCall.success() && failCallsToday >= 3) {
            return "최근 연속 실패 -> fail_reason 확인 (RATE_LIMIT=팀 쿼터 소진, AUTH_INVALID=키 설정 문제, BAD_REQUEST=코드 문제)";
        }
        return "정상 동작 중";
    }
}