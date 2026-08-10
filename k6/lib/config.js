export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const RESULT_NAME = __ENV.RESULT_NAME || 'k6-result';

export const STUDENT_TOKENS = [
    __ENV.TOKEN_STUDENT1 || 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJlOTRhZjYyMy02MGFjLTRmMDYtOGJiMS04ZjM0NmQzNzlkNjYiLCJzdWIiOiIyIiwidHlwZSI6IkFDQ0VTUyIsImlhdCI6MTc4NjMzNjQwNiwiZXhwIjoxNzg2MzQwMDA2fQ.6HIyNXuo3us_r136PaLuOsw4Sm1TaftWp3WrdxD5ZsPx9Wa5tZTAHO5UP1AzNY0eqBiLOsLyjEM1IU555sfFZA',
    __ENV.TOKEN_STUDENT2 || 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJjOTM0NTFmYS1hZTQzLTRjNTItOTMzNS02ZjkzZjQwNGY2MTMiLCJzdWIiOiIzIiwidHlwZSI6IkFDQ0VTUyIsImlhdCI6MTc4NjMzNjQzOCwiZXhwIjoxNzg2MzQwMDM4fQ.tdG6QNIPzPH27YH4AcbQZsB3jDjW-W3T0LuZoYSZp8eNUI4H6Dopcy9_25PBDGwAuwaZuFGDGQxv1JnuYumtWg',
    // ...
];

export const MIN_SLEEP_SECONDS = Number(__ENV.MIN_SLEEP_SECONDS || 0.5);
export const MAX_SLEEP_SECONDS = Number(__ENV.MAX_SLEEP_SECONDS || 1.5);

export function randomSleepSeconds() {
    return Math.random() * (MAX_SLEEP_SECONDS - MIN_SLEEP_SECONDS) + MIN_SLEEP_SECONDS;
}


// 매니저/강사 계정 JWT 토큰 목록입니다.
// 캐시 스탬피드 시나리오에서 같은 bootcamp 소속 여러 관리자 역할로 동시 요청을 재현합니다.
export const MANAGER_TOKENS = [
    __ENV.TOKEN_MANAGER1 || 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiI4NWRmZDE5Zi1hNWY1LTRiMTAtYWY5Ny0wNmYyZGExYWU4MjYiLCJzdWIiOiI0MyIsInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3ODYzMzY0NzQsImV4cCI6MTc4NjM0MDA3NH0._PWUXYXCclyNzlUyM2mG-e4dlz-iu9X3Kp8uoJKuld2Bh-abu3QjD5CT538I6D_rtZAxgF-2Yx6UcvyZU5Pbjw',
    __ENV.TOKEN_INSTRUCTOR1 || 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJiYjA3MjBkZS1iMmFmLTQ4ZjMtYWNmNS0yNTdkNWUyNDRhYzEiLCJzdWIiOiI0MSIsInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3ODYzMzY1MjAsImV4cCI6MTc4NjM0MDEyMH0.JvxoC_JNLTEDf-vaK22pIfq9_vJIcBO7-RfpXiNH0ZZiMR0An4LyWHP7ln9_Ntp4lw3k3T3TrYF4SnmaA0QL9w',
    __ENV.TOKEN_INSTRUCTOR2 || 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIzODRiNDUxYi0wYWNkLTRmZmMtOGZmNi05MDczYjBjNDFlY2YiLCJzdWIiOiI0MiIsInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3ODYzMzY1NTAsImV4cCI6MTc4NjM0MDE1MH0.nRNe6gYH5Yhl-byTxxdLej1m7uPAMV65pRWo7J__PBNEVWAqDO9pp-VETH8m_AUtDVg73p0TzFvzDqXS66uo_w',
];

export function randomManagerToken() {
    return MANAGER_TOKENS[Math.floor(Math.random() * MANAGER_TOKENS.length)];
}

// 출석 추이 조회 시 사용할 단위기간 ID입니다.
// 개선 전/후 비교 시 동일 조건을 유지하기 위해 고정값을 씁니다.
export const FIXED_PERIOD_ID = Number(__ENV.FIXED_PERIOD_ID || 0) || null;