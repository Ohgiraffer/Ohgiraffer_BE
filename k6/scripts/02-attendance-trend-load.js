// // k6/scripts/02-attendance-trend-load.js
// // ------------------------------------------------------------
// // 목적:
// // - present-absent/count(출석 추이) API는 호출마다 getAttendancePeriods()를
// //   캐싱 없이 재조회합니다. 동시 요청이 늘어날 때 이 부분이 병목인지 확인합니다.
// // - periodId를 매번 생략(기본 로직 타게)했을 때와 고정값으로 넘겼을 때를 비교해
// //   "오늘 속한 단위기간 탐색" 로직 자체의 비용도 별도로 볼 수 있습니다.
// //
// // 관찰할 것:
// // - k6: attendance-trend 타입 p95 (periodId 유무에 따른 태그 비교)
// // - Prometheus: getAttendancePeriods 관련 쿼리 반복 호출 횟수
// // - Loki: 동일 bootcampId로 반복되는 단위기간 조회 쿼리 로그
// //
// // 실행:
// //   k6 run k6/scripts/04-attendance-trend-load.js
// //   FIXED_PERIOD_ID=3 k6 run k6/scripts/04-attendance-trend-load.js
// // ------------------------------------------------------------
//
// import { sleep } from 'k6';
// import { getAttendanceTrend } from '../lib/attendance-client.js';
// import { randomManagerToken, randomSleepSeconds, FIXED_PERIOD_ID } from '../lib/config.js';
// import { createSummaryHandler } from '../lib/summary.js';
//
// export const options = {
//     summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
//     scenarios: {
//         trend_read_load: {
//             executor: 'ramping-vus',
//             stages: [
//                 { duration: '10s', target: 30  },  // 워밍업
//                 { duration: '30s', target: 80  },  // 유지
//                 { duration: '10s', target: 0   },
//             ],
//             gracefulRampDown: '10s',
//         },
//     },
//     thresholds: {
//         http_req_failed: ['rate<0.01'],
//         'http_req_duration{type:attendance-trend}': ['p(95)<1000'],
//     },
// };
//
// export default function () {
//     const token = randomManagerToken();
//
//     // FIXED_PERIOD_ID가 없으면 매번 "오늘 속한 단위기간 탐색" 로직을 태웁니다.
//     getAttendanceTrend(token, FIXED_PERIOD_ID);
//
//     sleep(randomSleepSeconds());
// }
//
// export const handleSummary = createSummaryHandler('04-attendance-trend-load');

// k6/scripts/02-attendance-trend-load.js (강화 버전)
// ------------------------------------------------------------
// 변경점: 80 VU → 250 VU, 50초 → 90초
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getAttendanceTrend } from '../lib/attendance-client.js';
import { randomManagerToken, randomSleepSeconds, FIXED_PERIOD_ID } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        trend_read_load: {
            executor: 'ramping-vus',
            stages: [
                { duration: '15s', target: 80  },
                { duration: '15s', target: 180 },
                { duration: '30s', target: 250 },
                { duration: '30s', target: 0   },
            ],
            gracefulRampDown: '15s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.02'],
        'http_req_duration{type:attendance-trend}': ['p(95)<1000'],
    },
};

export default function () {
    const token = randomManagerToken();
    getAttendanceTrend(token, FIXED_PERIOD_ID);
    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('02-attendance-trend-load-heavy');