/**
 * notifyManager.js
 *
 * 개발자 Slack 알림과는 별도로, 매니저 채널에 "비개발자가 이해할 수 있는" 요약을 보낸다.
 *
 * 순서 원칙: 매니저 알림은 항상 "Slack으로 개발팀에 통보/갱신이 끝난 뒤"에만 나간다.
 * 감지 시점에 매니저한테 먼저(또는 동시에) 알리는 경로는 없다 - 개발팀이 먼저 알고,
 * 해결됐는지/실패했는지가 결정된 다음에만 매니저 채널로 결과가 전달된다.
 *
 * 훅 포인트 (server.js, 매번 sendToSlack/respondToSlack 호출 *다음*에 위치):
 *   - handleSingleAlert()의 AUTO_EXECUTE 분기: sendToSlack() 이후
 *   - handleSingleApproval(): respondToSlack() 이후
 *   - handleTeamApproval(): respondToSlack() 이후 (거부/정족수달성 양쪽 분기)
 *   → notifyManagerOnResolve({ alertName, policy, result })  ← 성공/실패/거부 모두 호출
 *
 * 참고: actions.js의 riskTier는 LOW/MEDIUM/HIGH 3단계뿐이고 MAX는 없다.
 * HIGH는 정의상 TEAM_APPROVAL 경로와 1:1 대응이므로 그대로 에스컬레이션 기준으로 쓴다.
 * HIGH여도 "감지 즉시"는 없고, 결과가 나온 시점에만 즉시 발송한다.
 */

const { summarizeForManager } = require("./gemini");
const { sendMessageToManagerChannel } = require("./sendbird");

// ── 설정값 ──────────────────────────────────────────────────────
// riskTier는 별도로 재계산하지 않고 evaluateAlert()가 돌려주는 finalTier를 그대로 재사용한다.
// (Impact 축을 따로 계산하면 두 지표가 어긋날 때 어느 걸 믿어야 하는지 혼란이 생김)
const ESCALATION_TIERS = ["HIGH"]; // actions.js TIER 기준 (LOW/MEDIUM/HIGH). MAX는 이 코드베이스에 없음

// 매니저 요약 프롬프트에 넘길 필드는 최소 집합으로 시작한다.
// 여기 없는 필드(labels, instanceId, IP, 토큰 등)는 절대 프롬프트에 들어가지 않는다.
const MANAGER_REPORT_WHITELIST = [
  "alertName",
  "summary", // analysis.발생 (또는 기본 문구)
  "riskTier", // policy.finalTier
  "actionTaken", // policy.action
  "resolvedAt", // 해결 시각 (미해결 시 null)
  "timestamp", // 알림 생성 시각
];


// 다이제스트 배치 시각은 로직에 영향을 주지 않는 값이라 상수로만 빼둔다.
// 나중에 바꿔도 코드 구조가 흔들리지 않도록 cron 표현식만 수정하면 됨.
const DIGEST_CRON = "0 9 * * *"; // 매일 오전 9시

// 다이제스트 대기열. 프로세스 재시작 시 유실되는 게 부담이면 Redis로 옮길 것.
const digestQueue = [];

// ── 화이트리스트 필터링 ─────────────────────────────────────────
/**
 * server.js에서 이미 갖고 있는 값들(alertName, policy, analysis, result)에서
 * 화이트리스트 필드만 뽑아 매니저 리포트용 객체로 만든다.
 * Gemini 프롬프트에 넘기기 전 단계이므로, 여기서 걸러지지 않으면
 * 요약 결과에도 절대 노출될 수 없다.
 */
function sanitizeForManagerReport({ alertName, policy, analysis, result }) {
  const merged = {
    alertName,
    summary: (analysis && analysis.발생) || `${alertName} 알럿이 발생했습니다.`,
    riskTier: policy ? policy.finalTier : null,
    actionTaken: policy ? policy.action : null,
    resolvedAt: result ? new Date().toISOString() : null,
    timestamp: new Date().toISOString(),
  };

  const sanitized = {};
  for (const key of MANAGER_REPORT_WHITELIST) {
    if (merged[key] !== undefined) sanitized[key] = merged[key];
  }
  return sanitized;
}

// ── Gemini 요약 ─────────────────────────────────────────────────
/**
 * @param {object} sanitized - sanitizeForManagerReport()를 통과한 데이터만 넘길 것
 * @param {'DETECTED'|'RESOLVED'|'FAILED'} phase
 */
async function generateManagerSummary(sanitized, phase) {
  const phaseInstruction = {
    RESOLVED: "이미 해결된 이슈에 대한 결과 알림입니다.",
    FAILED: "조치가 실패했거나 팀에서 거부된 이슈입니다. 숨기지 말고 명확히 알리세요.",
  }[phase];

  const prompt = `
다음은 시스템 대응 로그입니다. 비개발자 매니저가 이해할 수 있게
"무슨 문제였는지 → 어떻게 조치했는지(또는 조치 중인지) → 지금 상태"를
3줄 이내 존댓말로 요약하세요. 기술 용어(DB, 커넥션풀, 인스턴스 등) 대신
일반적인 표현을 쓰세요. ${phaseInstruction}

발생 시각: ${sanitized.timestamp}
문제 요약: ${sanitized.summary}
위험 등급: ${sanitized.riskTier}
조치 내용: ${sanitized.actionTaken || "(진행 중)"}
해결 시각: ${sanitized.resolvedAt || "(미해결)"}
  `.trim();

  return summarizeForManager(prompt);
}

// ── 전송 ────────────────────────────────────────────────────────
// Sendbird 봇 명의로 고정된 매니저 채널(MANAGER_CHANNEL_URL)에 전송한다.
// 채널 생성/봇 유저 생성은 scripts/setup-manager-channel.js에서 최초 1회만 처리.
async function sendToManagerChannel(text) {
  await sendMessageToManagerChannel(text);
}

// ── 진입점: 실행 결과가 나오는 시점 (성공/실패/거부 모두 호출) ──
// 반드시 sendToSlack/respondToSlack로 개발팀 통보가 끝난 *다음*에 호출할 것.
// server.js의 세 곳(AUTO_EXECUTE 실행 직후, handleSingleApproval, handleTeamApproval)에서 호출
async function notifyManagerOnResolve({ alertName, policy, analysis, result }) {
  const phase = result.success ? "RESOLVED" : "FAILED";

  if (!ESCALATION_TIERS.includes(policy.finalTier)) {
    queueForDigest({ alertName, policy, analysis, result }, phase);
    return;
  }

  const sanitized = sanitizeForManagerReport({ alertName, policy, analysis, result });
  const summary = await generateManagerSummary(sanitized, phase);
  const icon = result.success ? "✅" : "⚠️";
  await sendToManagerChannel(`${icon} [처리 결과]\n${summary}`);
}

// ── 다이제스트 대기열 ────────────────────────────────────────────
function queueForDigest(payload, phase) {
  digestQueue.push({
    sanitized: sanitizeForManagerReport(payload),
    phase,
    queuedAt: new Date().toISOString(),
  });
  console.log(`[notifyManager] 다이제스트 큐 적재 | 현재 대기건수=${digestQueue.length} | alertName=${payload.alertName} | phase=${phase}`);
}

/**
 * 매일 지정 시각(DIGEST_CRON)에 호출. 대기열을 통째로 Gemini에 넘겨서
 * 한 번의 요약으로 압축한다 (건별 요약을 이어붙이면 매니저가 읽기 피로해짐).
 */
async function sendDailyDigest() {
  if (digestQueue.length === 0) return;

  const items = digestQueue.splice(0, digestQueue.length); // 전송 전에 비우기 (재시도 중복 방지)

  const prompt = `
다음은 오늘 하루 동안 있었던 경미한 시스템 이슈 목록입니다.
비개발자 매니저용으로 "총 몇 건, 자동/승인 처리로 해결된 건 몇 건, 특이사항 있으면 한 줄"
형태로 5줄 이내 존댓말 요약을 작성하세요.

${JSON.stringify(items.map((i) => i.sanitized), null, 2)}
  `.trim();

  const summary = await summarizeForManager(prompt);
  await sendToManagerChannel(`📋 [오늘의 처리 현황]\n${summary}`);
}

module.exports = {
  notifyManagerOnResolve,
  sendDailyDigest,
  sanitizeForManagerReport, // 테스트에서 화이트리스트 검증용으로 export
  DIGEST_CRON,
};
