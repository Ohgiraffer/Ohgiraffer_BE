/**
 * 위험 등급 판정 정책
 * 이 파일은 docs/action-risk-policy.md 문서를 그대로 구현한 것.
 * 등급 기준을 바꿀 때는 문서와 이 파일을 함께 수정할 것.
 */

const TIER = {
  LOW: "LOW",
  MEDIUM: "MEDIUM",
  HIGH: "HIGH",
};

const TIER_ORDER = { LOW: 0, MEDIUM: 1, HIGH: 2 };

// 등급과 무관하게 항상 고위험으로 취급하는 조치 키워드 (하드 룰)
const ALWAYS_HIGH_RISK_KEYWORDS = [
  "롤백",
  "재배포",
  "스키마 변경",
  "데이터 삭제",
  "데이터 수정",
  "권한 변경",
  "인증 설정",
  "방화벽",
  "트래픽 라우팅",
];

/**
 * 알럿 이름 -> { severity(알럿 심각도), action(기본 권장 조치), actionRisk(조치 위험도) }
 * docs/action-risk-policy.md 4번 표와 반드시 동일하게 유지.
 */
const ALERT_POLICY = {
  "DB Connection Pool Saturation": {
    severity: TIER.LOW, // 일시적 스파이크가 잦고 리셋만으로 자연 회복되는 신호로 판단
    action: "HikariCP 커넥션 풀 리셋",
    actionRisk: TIER.LOW,
    autoExecutable: true,
    scriptId: "reset-db-connection-pool", // 화이트리스트 스크립트 식별자 (아직 실제 실행 로직은 없음, mock)
  },
  "JVM Heap Memory High Usage": {
    severity: TIER.MEDIUM,
    action: "애플리케이션 프로세스 재시작",
    actionRisk: TIER.MEDIUM,
    autoExecutable: false,
    scriptId: "restart-app-process",
  },
  "HTTP 5xx Error Rate Spike": {
    severity: TIER.HIGH,
    action: "원인 특정 전까지 자동 조치 불가 (코드/배포 이슈 가능성)",
    actionRisk: TIER.HIGH,
    autoExecutable: false,
    scriptId: null,
    requiredApprovals: 2, // 팀 전체 승인: 2명 이상 승인해야 "승인 완료" 처리
  },
};

// 정책표에 등록되지 않은 알럿은 안전하게 고위험으로 처리 (가이드라인 5번 "향후 확장" 원칙)
const DEFAULT_POLICY = {
  severity: TIER.HIGH,
  action: "정책이 등록되지 않은 알럿입니다. 팀 검토가 필요합니다.",
  actionRisk: TIER.HIGH,
  autoExecutable: false,
  scriptId: null,
  requiredApprovals: 2,
};

function maxTier(a, b) {
  return TIER_ORDER[a] >= TIER_ORDER[b] ? a : b;
}

function containsHardRuleKeyword(actionText) {
  if (!actionText) return false;
  return ALWAYS_HIGH_RISK_KEYWORDS.some((kw) => actionText.includes(kw));
}

/**
 * 알럿 이름을 받아 최종 등급과 처리 경로를 판정한다.
 * 반환값: { finalTier, route, severity, actionRisk, action, autoExecutable, scriptId }
 */
function evaluateAlert(alertName) {
  const policy = ALERT_POLICY[alertName] || DEFAULT_POLICY;

  let actionRisk = policy.actionRisk;
  // 하드 룰: 조치 텍스트에 위험 키워드가 있으면 무조건 HIGH로 강제
  if (containsHardRuleKeyword(policy.action)) {
    actionRisk = TIER.HIGH;
  }

  const finalTier = maxTier(policy.severity, actionRisk);

  const route =
    finalTier === TIER.LOW
      ? "AUTO_EXECUTE"
      : finalTier === TIER.MEDIUM
      ? "SINGLE_APPROVAL"
      : "TEAM_APPROVAL";

  return {
    finalTier,
    route,
    severity: policy.severity,
    actionRisk,
    action: policy.action,
    autoExecutable: finalTier === TIER.LOW && policy.autoExecutable,
    scriptId: policy.scriptId,
    requiredApprovals: policy.requiredApprovals || 2,
  };
}

module.exports = { TIER, evaluateAlert, ALERT_POLICY, DEFAULT_POLICY };
