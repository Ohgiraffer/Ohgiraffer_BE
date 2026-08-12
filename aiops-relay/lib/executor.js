/**
 * 화이트리스트 기반 조치 실행기.
 *
 * 절대 원칙: 여기 정의된 scriptId 외에는 아무것도 실행할 수 없다.
 * AI나 사용자 입력으로 임의의 명령/스크립트를 실행하는 경로는 만들지 않는다.
 * (config/actions.js의 ALERT_POLICY에서 scriptId를 지정하고, 그 값이 여기 없으면 실행 자체가 거부됨)
 */

const BACKEND_ADMIN_URL = process.env.BACKEND_ADMIN_URL; // 예: https://be.campflow.co.kr
const ADMIN_INTERNAL_TOKEN = process.env.ADMIN_INTERNAL_TOKEN;

// 실행 이력 (데모용 인메모리 기록. 서버 재시작하면 초기화됨)
const executionHistory = [];

/**
 * scriptId -> 실제 실행 함수 매핑.
 * 각 함수는 { success, message } 형태를 반환해야 함.
 */
const WHITELIST = {
  "reset-db-connection-pool": async (context) => {
    if (!BACKEND_ADMIN_URL || !ADMIN_INTERNAL_TOKEN) {
      return {
        success: false,
        message:
          "BACKEND_ADMIN_URL 또는 ADMIN_INTERNAL_TOKEN이 설정되지 않아 실제 호출을 건너뜁니다. (.env 확인 필요)",
      };
    }

    try {
      const res = await fetch(`${BACKEND_ADMIN_URL}/admin/reset-connection-pool`, {
        method: "POST",
        headers: {
          "X-Internal-Token": ADMIN_INTERNAL_TOKEN,
          "Content-Type": "application/json",
        },
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        return {
          success: false,
          message: `백엔드 응답 실패 (HTTP ${res.status}): ${data.message || "알 수 없는 오류"}`,
        };
      }

      return {
        success: true,
        message: `${data.message} (리셋 전 idle 커넥션: ${data.idleConnectionsBeforeReset}개)`,
      };
    } catch (err) {
      return { success: false, message: `백엔드 호출 중 네트워크 오류: ${err.message}` };
    }
  },

  "restart-app-process": async (context) => {
    // 이건 아직 실제 엔드포인트가 없어서 mock 유지.
    // 실제 재시작은 위험도가 더 높은 조치라, 이후 별도로 안전장치를 갖춰서 연결할 것.
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
