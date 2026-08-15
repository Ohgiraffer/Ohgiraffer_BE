package com.ohgiraffer.aiops.infrastructure.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  팀원 도메인(team_outbox)의 실패 건수를 코드 수정 없이 외부에서 관찰하기 위한 메트릭 바인더.
 *  team 모듈의 엔티티/리포지토리에 전혀 의존하지 않고, 공유 테이블에 대한 읽기 전용
 *  native query만 사용한다 - 도메인 경계를 넘지 않으면서 관찰만 하는 방식.
 *  -
 *  배경: TeamOutboxRetryScheduler가 재시도 횟수 상한/dead-letter/영구 실패 알림 없이
 *  60초마다 FAILED 건을 계속 재시도만 하고 있어서, 이 건수가 CloudWatch 알람의
 *  1순위 후보다 (재시도만으로 안 줄어들면 담당자가 알아야 하는 상황).
 *  -
 *  Gauge는 pull 방식이라 별도 스케줄러 없이, CloudWatch export 주기(step)마다
 *  아래 쿼리가 자동으로 실행된다.
 */

@Component
@RequiredArgsConstructor
public class OutboxMetricsBinder implements MeterBinder {

    private static final String COUNT_FAILED_OUTBOX_SQL =
            "SELECT COUNT(*) FROM team_outbox WHERE status = 'FAILED'";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("team.outbox.failed.count", this, OutboxMetricsBinder::countFailedOutbox)
                .description("team_outbox 테이블에서 FAILED 상태로 남아있는 건수 (재시도 상한이 없어 무한 재시도 중일 수 있음)")
                .register(registry);
    }

    private double countFailedOutbox() {
        Integer count = jdbcTemplate.queryForObject(COUNT_FAILED_OUTBOX_SQL, Integer.class);
        return count == null ? 0 : count;
    }

}
