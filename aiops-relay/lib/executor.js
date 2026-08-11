/**
 * 화이트리스트 기반 조치 실행기.
 *
 * 절대 원칙: 여기 정의된 scriptId 외에는 아무것도 실행할 수 없다.
 * AI나 사용자 입력으로 임의의 명령/스크립트를 실행하는 경로는 만들지 않는다.
 * (config/actions.js의 ALERT_POLICY에서 scriptId를 지정하고, 그 값이 여기 없으면 실행 자체가 거부됨)
 *
 * 지금은 데모/개발 단계라 실제 인프라를 건드리지 않는 mock 함수로 구현.
 * 나중에 실제 조치(재시작 API 호출 등)로 교체할 때도 이 파일의 함수 시그니처만 유지하면 됨.
 */

// 실행 이력 (데모용 인메모리 기록. 서버 재시작하면 초기화됨)
const executionHistory = [];

/**
 * scriptId -> 실제 실행 함수 매핑.
 * 각 함수는 { success, message } 형태를 반환해야 함.
 */
const WHITELIST = {
  "reset-db-connection-pool": async (context) => {
    // 실제로는 여기서 백엔드의 관리용 엔드포인트를 호출해 HikariCP 풀을 리셋하게 됨.
    // 지금은 mock: 실제 인프라를 건드리지 않고 성공했다고 가정.
    await sleep(300); // 실제 API 호출을 흉내내는 지연
    return {
      success: true,
      message: `[MOCK] ${context.alertName} - DB 커넥션 풀 리셋 명령을 전송했습니다. (실제 호출 아님, 데모용)`,
    };
  },

  "restart-app-process": async (context) => {
    // 이건 정책상 MEDIUM(1인승인) 등급이라 지금 단계에서는 자동 실행 경로를 안 탐.
    // 화이트리스트에는 등록해두되, 향후 승인 로직과 연결될 때 사용.
    await sleep(300);
    return {
      success: true,
      message: `[MOCK] ${context.alertName} - 애플리케이션 재시작 명령을 전송했습니다. (실제 호출 아님, 데모용)`,
    };
  },
};

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * scriptId로 조치를 실행한다. 화이트리스트에 없으면 절대 실행하지 않고 명시적으로 거부한다.
 */
async function runAction(scriptId, context) {
  if (!scriptId) {
    return { success: false, message: "실행할 스크립트가 지정되지 않았습니다." };
  }

  const fn = WHITELIST[scriptId];
  if (!fn) {
    console.error(`[executor] 화이트리스트에 없는 scriptId 실행 시도 차단: ${scriptId}`);
    return { success: false, message: `허용되지 않은 조치입니다: ${scriptId} (실행 거부됨)` };
  }

  const startedAt = new Date().toISOString();
  try {
    const result = await fn(context);
    logExecution({ scriptId, context, result, startedAt, ok: true });
    return result;
  } catch (err) {
    const result = { success: false, message: `실행 중 오류: ${err.message}` };
    logExecution({ scriptId, context, result, startedAt, ok: false });
    return result;
  }
}

function logExecution(entry) {
  executionHistory.push(entry);
  console.log(`[executor] 실행 기록:`, JSON.stringify(entry));
}

function getExecutionHistory() {
  return executionHistory;
}

module.exports = { runAction, getExecutionHistory, WHITELIST };
