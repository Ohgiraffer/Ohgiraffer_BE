// k6/scripts/00-attendance-smoke-check.js
// ------------------------------------------------------------
// 목적:
// - 출결 도메인 부하테스트 전에 서버/권한/데이터 세팅이 정상인지 확인합니다.
// - 특히 관리자 권한(MANAGER/INSTRUCTOR)과 "오늘이 속한 단위기간" 존재 여부를
//   본 테스트 전에 미리 검증합니다. (없으면 present-absent/count가 404)
//
// 실행:
//   k6 run k6/scripts/00-attendance-smoke-check.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getAttendanceList, getDashboardSummary, getAttendanceTrend } from '../lib/attendance-client.js';
import { randomManagerToken, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    vus: 1,
    iterations: 3,
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<2000'],
    },
};

export default function () {
    const token = randomManagerToken();

    // 1. 훈련생 출결 목록 조회로 DB 연결과 권한(MANAGER/INSTRUCTOR)을 확인합니다.
    getAttendanceList(token);

    // 2. 대시보드 요약 조회로 캐시 레이어(AttendanceDashboardCache)가
    //    정상적으로 응답을 만들어내는지 확인합니다.
    getDashboardSummary(token);

    // 3. 출석 추이 조회로 "오늘이 속한 단위기간"이 DB에 세팅돼 있는지 확인합니다.
    //    없으면 404(ATTENDANCE_PERIOD_NOT_FOUND)가 나므로 본 테스트 전에 걸러냅니다.
    getAttendanceTrend(token);

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('00-attendance-smoke-check');