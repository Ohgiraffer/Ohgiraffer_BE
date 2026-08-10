require("dotenv").config();
const express = require("express");
const { fetchRecentLogs } = require("./lib/loki");
const { analyzeAlert } = require("./lib/gemini");
const { sendToSlack } = require("./lib/slack");
const { evaluateAlert } = require("./config/actions");
const { runAction } = require("./lib/executor");

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 4000;

/**
 * Grafana Alerting > Contact points 에서
 * Integration: Webhook, URL: http://<이-서버>:4000/webhook/grafana 로 등록하면
 * 알럿이 firing/resolved 될 때마다 이 엔드포인트로 POST가 온다.
 */
app.post("/webhook/grafana", async (req, res) => {
  // Grafana가 응답을 오래 기다리지 않도록 먼저 200을 반환하고,
  // 실제 처리(로그 조회 + LLM 호출 + Slack 전송)는 비동기로 뒤에서 진행한다.
  res.status(200).json({ ok: true });

  const body = req.body;
  const alerts = body?.alerts || [];

  if (alerts.length === 0) {
    console.log("[webhook] alerts 배열이 비어있음, 처리할 것 없음");
    return;
  }

  for (const alert of alerts) {
    try {
      await handleSingleAlert(alert);
    } catch (err) {
      // 알럿 하나 처리 실패가 다른 알럿 처리에 영향 주지 않도록 개별 try/catch
      console.error("[webhook] 알럿 처리 중 오류:", err);
    }
  }
});

async function handleSingleAlert(alert) {
  const status = alert.status || "unknown"; // "firing" | "resolved"
  const labels = alert.labels || {};
  const alertName = labels.alertname || "이름없는알럿";
  const value = formatValues(alert.values) || "값 없음";

  console.log(`[webhook] 처리 시작: ${alertName} (${status})`);

  // resolved 상태는 굳이 AI 분석/로그 조회까지 할 필요 없이 간단히 알려주면 충분
  if (status === "resolved") {
    await sendToSlack({
      alertName,
      status,
      value,
      labels,
      analysis: {
        발생: `${alertName} 알럿이 해소되었습니다.`,
        why: "-",
        how: "정상 상태로 복귀했습니다. 추가 조치가 필요하지 않습니다.",
      },
    });
    return;
  }

  // firing 상태만 실제 AI 분석까지 진행
  const logs = await fetchRecentLogs();
  console.log(`[webhook] 로그 ${logs.length}줄 수집됨`);

  const analysis = await analyzeAlert({ alertName, status, value, labels, logs });
  console.log("[webhook] AI 분석 완료:", analysis);

  // 위험 등급 판정 (config/actions.js, docs/action-risk-policy.md 참고)
  const policy = evaluateAlert(alertName);
  console.log("[webhook] 등급 판정:", policy);

  let executionResult = null;
  if (policy.route === "AUTO_EXECUTE" && policy.autoExecutable) {
    console.log(`[webhook] 저위험 판정 → 자동 실행 시도: ${policy.scriptId}`);
    executionResult = await runAction(policy.scriptId, { alertName, value, labels });
    console.log("[webhook] 자동 실행 결과:", executionResult);
  }

  await sendToSlack({ alertName, status, value, labels, analysis, policy, executionResult });
  console.log(`[webhook] 처리 완료: ${alertName} (경로: ${policy.route})`);
}

function formatValues(values) {
  if (!values) return null;
  return Object.entries(values)
    .map(([k, v]) => `${k}=${v}`)
    .join(", ");
}

// 헬스체크 (도커/모니터링에서 살아있는지 확인용)
app.get("/health", (req, res) => res.json({ status: "ok" }));

app.listen(PORT, () => {
  console.log(`AIOps 중계서버가 http://localhost:${PORT} 에서 실행 중`);
  console.log(`Grafana webhook 등록 주소: http://<이-서버-IP>:${PORT}/webhook/grafana`);
});
