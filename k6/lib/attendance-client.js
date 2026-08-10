// k6/lib/attendance-client.js
// ------------------------------------------------------------
// CampFlow 출결 관리 API 호출을 함수로 감싼 파일입니다.
// client.js와 동일한 관례: HTTP 세부는 여기서, 시나리오는 행동만.
// ------------------------------------------------------------

import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL } from './config.js';

function authHeaders(token) {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
}

// 관리자용 출결 대시보드 요약 조회입니다.
// AttendanceDashboardCache를 태우는 API로, bootcampId+오늘날짜 키를 씁니다.
export function getDashboardSummary(token) {
    const res = http.get(`${BASE_URL}/attendance/dashboard-summary`, {
        headers: authHeaders(token),
        tags: { type: 'dashboard-summary', api: 'GET /attendance/dashboard-summary' },
    });

    check(res, {
        'dashboard-summary: status is 200': (r) => r.status === 200,
        'dashboard-summary: no server error': (r) => r.status !== 500,
    });

    return res;
}

// 관리자용 훈련생 출결 목록 조회입니다.
// AttendanceListCache를 태웁니다. 대시보드와 같은 캐시 키 패턴(bootcampId+오늘날짜)을 씁니다.
export function getAttendanceList(token) {
    const res = http.get(`${BASE_URL}/attendance/list`, {
        headers: authHeaders(token),
        tags: { type: 'attendance-list', api: 'GET /attendance/list' },
    });

    check(res, {
        'attendance-list: status is 200': (r) => r.status === 200,
        'attendance-list: no server error': (r) => r.status !== 500,
    });

    return res;
}

// 단위기간별 출석 추이 조회입니다.
// periodId를 생략하면 매 호출마다 getAttendancePeriods()를 캐시 없이 재조회합니다.
// (개선 전/후 비교용 API)
export function getAttendanceTrend(token, periodId = null) {
    let url = `${BASE_URL}/attendance/present-absent/count`;
    if (periodId) url += `?periodId=${periodId}`;

    const res = http.get(url, {
        headers: authHeaders(token),
        tags: { type: 'attendance-trend', api: 'GET /attendance/present-absent/count' },
    });

    check(res, {
        'attendance-trend: status is 200': (r) => r.status === 200,
        'attendance-trend: no server error': (r) => r.status !== 500,
    });

    return res;
}

// 관리자가 대시보드 화면을 켰을 때의 실제 행동 패턴입니다.
// list -> dashboard-summary -> trend 순으로 3개 API를 거의 동시에 쏩니다.
// (실제 프론트가 화면 진입 시 병렬로 3개를 호출하는 상황 재현)
export function managerOpenDashboard(token) {
    getAttendanceList(token);
    getDashboardSummary(token);
    getAttendanceTrend(token);
}