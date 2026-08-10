// // k6/scripts/01-dashboard-cache-spike.js
// // ------------------------------------------------------------
// // 목적:
// // - AttendanceDashboardCache / AttendanceListCache의 캐시 미스 순간(자정 롤오버 직후)에
// //   같은 bootcamp 소속 매니저/강사들이 동시에 첫 조회를 하는 상황을 재현합니다.
// // - 캐시 스탬피드 발생 시 DB 재계산이 얼마나 동시에 몰리는지, latency가 얼마나 튀는지 확인합니다.
// //
// // 관찰할 것:
// // - k6: dashboard-summary / attendance-list 타입 p95, p99 (spike 구간 vs steady 구간 비교)
// // - Prometheus: DB 커넥션 풀 active count, 캐시 hit/miss 비율
// // - Loki: 같은 시각에 몰리는 캐시 재계산 쿼리 로그
// //
// // 실행:
// //   k6 run k6/scripts/03-dashboard-cache-spike.js
// //   RESULT_NAME=dashboard-spike-before k6 run k6/scripts/03-dashboard-cache-spike.js
// // ------------------------------------------------------------
//
// import { sleep } from 'k6';
// import { managerOpenDashboard } from '../lib/attendance-client.js';
// import { randomManagerToken, randomSleepSeconds } from '../lib/config.js';
// import { createSummaryHandler } from '../lib/summary.js';
//
// export const options = {
//     summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
//     scenarios: {
//         // steady_baseline: 캐시가 warm한 상태에서의 평상시 조회량입니다.
//         // spike 구간과 비교하기 위한 기준선입니다.
//         steady_baseline: {
//             executor: 'constant-vus',
//             vus: 5,
//             duration: '30s',
//             startTime: '0s',
//         },
//         // cache_miss_spike: 캐시가 비어있다고 가정하고 짧은 시간에 다수 요청을 몰아넣습니다.
//         // 자정 롤오버 직후 여러 관리자가 동시에 화면을 켜는 상황을 재현합니다.
//         cache_miss_spike: {
//             executor: 'ramping-vus',
//             startTime: '30s',
//             stages: [
//                 { duration: '5s',  target: 40 },  // 순간적으로 40명 동시 진입
//                 { duration: '15s', target: 40 },  // 유지 (재계산 완료까지 관찰)
//                 { duration: '5s',  target: 0  },
//             ],
//             gracefulRampDown: '5s',
//         },
//     },
//     thresholds: {
//         http_req_failed: ['rate<0.01'],
//         // 캐시가 정상 동작한다면 spike 구간이라도 p95가 급격히 튀지 않아야 합니다.
//         'http_req_duration{type:dashboard-summary}': ['p(95)<1500'],
//         'http_req_duration{type:attendance-list}':   ['p(95)<1500'],
//     },
// };
//
// export default function () {
//     const token = randomManagerToken();
//     managerOpenDashboard(token);
//     sleep(randomSleepSeconds());
// }
//
// export const handleSummary = createSummaryHandler('03-dashboard-cache-spike');

// k6/scripts/01-dashboard-cache-spike.js (강화 버전)
// ------------------------------------------------------------
// 변경점 (기존 대비):
// - steady_baseline: 5 VU → 15 VU, 30초 → 60초
// - cache_miss_spike: 40 VU → 150 VU, 급격한 램프업(5초 안에 스파이크)
// - 총 실행 시간 증가로 로컬 환경(DB 커넥션 풀, CPU)의 실제 한계 탐색
// ------------------------------------------------------------

import { sleep } from 'k6';
import { managerOpenDashboard } from '../lib/attendance-client.js';
import { randomManagerToken, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        steady_baseline: {
            executor: 'constant-vus',
            vus: 15,
            duration: '60s',
            startTime: '0s',
        },
        cache_miss_spike: {
            executor: 'ramping-vus',
            startTime: '60s',
            stages: [
                { duration: '5s',  target: 150 },  // 5초 안에 150명까지 급격히 몰림
                { duration: '30s', target: 150 },  // 유지하며 재계산 완료까지 관찰
                { duration: '10s', target: 0   },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.02'],
        'http_req_duration{type:dashboard-summary}': ['p(95)<2000'],
        'http_req_duration{type:attendance-list}':   ['p(95)<2000'],
    },
};

export default function () {
    const token = randomManagerToken();
    managerOpenDashboard(token);
    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('01-dashboard-cache-spike-heavy');