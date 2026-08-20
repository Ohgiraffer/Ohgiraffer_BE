package com.ohgiraffer.global.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CampFlowMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> gauges = new ConcurrentHashMap<>();

    public CampFlowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // ===== Timer =====
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String metricName) {
        sample.stop(meterRegistry.timer(metricName));
    }

    // ===== Counter =====
    public void incrementCounter(String metricName) {
        meterRegistry.counter(metricName).increment();
    }

    public void incrementCounter(String metricName, String... tags) {
        meterRegistry.counter(metricName, tags).increment();
    }

    // ===== Gauge =====
    // 현재 값을 그때그때 갱신하는 지표. ex) 캐시에 남은 키 개수, 결재 대기 건수
    // AtomicInteger를 미리 registry에 등록해두고, set()으로 값만 갱신하는 방식
    public void setGauge(String metricName, int value) {
        gauges.computeIfAbsent(metricName, name ->
                meterRegistry.gauge(name, new AtomicInteger(0))
        ).set(value);
    }

    // ===== DistributionSummary =====
    // 시간이 아닌 값(크기, 개수, 바이트 등)의 분포를 기록. ex) 업로드 파일 크기, 배치 처리 row 수
    public void recordDistribution(String metricName, double amount) {
        DistributionSummary.builder(metricName)
                .register(meterRegistry)
                .record(amount);
    }

    // ===== LongTaskTimer =====
    // 지금 진행 중인 장시간 작업을 추적
    // 시작할 때 Sample을 반환하고 끝날 때 stop() 호출
    // Timer와 다르게 진행 중인 상태도 /actuator/metrics 로 실시간 조회 가능
    public LongTaskTimer.Sample startLongTask(String metricName) {
        LongTaskTimer longTaskTimer = LongTaskTimer.builder(metricName)
                .register(meterRegistry);
        return longTaskTimer.start();
    }
}