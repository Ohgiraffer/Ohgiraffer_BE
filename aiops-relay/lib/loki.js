const LOKI_URL = process.env.LOKI_URL || "http://localhost:3100";
const LABEL = process.env.LOKI_QUERY_LABEL || "container";
const VALUE = process.env.LOKI_QUERY_VALUE || "campflow-app";
const LOOKBACK_MIN = Number(process.env.LOG_LOOKBACK_MINUTES || 10);
const MAX_LINES = Number(process.env.LOG_MAX_LINES || 50);

/**
 * Loki에서 최근 N분간의 로그를 가져온다.
 * 실패해도 서버 전체가 죽지 않도록 에러를 삼키고 빈 배열을 반환한다.
 * (로그 컨텍스트는 "있으면 좋은 것"이지, 없다고 전체 파이프라인이 멈추면 안 됨)
 */
async function fetchRecentLogs() {
  const end = Date.now() * 1e6; // Loki는 나노초 단위 타임스탬프를 요구함
  const start = end - LOOKBACK_MIN * 60 * 1e9;

  const query = encodeURIComponent(`{${LABEL}="${VALUE}"}`);
  const url = `${LOKI_URL}/loki/api/v1/query_range?query=${query}&start=${start}&end=${end}&limit=${MAX_LINES}&direction=backward`;

  try {
    const res = await fetch(url);
    if (!res.ok) {
      console.error(`[loki] 조회 실패: HTTP ${res.status}`);
      return [];
    }
    const data = await res.json();
    const streams = data?.data?.result || [];

    const lines = [];
    for (const stream of streams) {
      for (const [, line] of stream.values || []) {
        lines.push(line);
      }
    }
    // Loki는 최신순으로 주므로, 시간순(오래된 것 -> 최신)으로 다시 정렬해서 보기 편하게
    return lines.reverse().slice(-MAX_LINES);
  } catch (err) {
    console.error("[loki] 조회 중 예외 발생:", err.message);
    return [];
  }
}

module.exports = { fetchRecentLogs };
