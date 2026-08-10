// k6/scripts/03-combined-heavy-load.js
// ------------------------------------------------------------
// 목적: 세 API를 동시에 최대 강도로 때려서 로컬 환경의 실제 한계(DB 커넥션 풀,
// CPU, Redis 처리량)를 찾는다. 지금까지 개별로 검증한 API들을 합쳐서
// 실제 대시보드 화면처럼 동시에 반복 새로고침하는 상황을 재현.
// ------------------------------------------------------------

import { sleep } from 'k6';
import { managerOpenDashboard } from '../lib/attendance-client.js';
import { randomManagerToken } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        heavy_combined: {
            executor: 'ramping-vus',
            stages: [
                { duration: '20s', target: 100 },
                { duration: '40s', target: 300 },  // 최대 강도
                { duration: '20s', target: 0   },
            ],
            gracefulRampDown: '15s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],  // 극한 부하이므로 임계치를 조금 완화
    },
};

export default function () {
    const token = randomManagerToken();
    managerOpenDashboard(token);
    // sleep 없이 최대한 몰아침 — 실제 사용자보다 훨씬 공격적인 패턴
}

export const handleSummary = createSummaryHandler('03-combined-heavy-load');