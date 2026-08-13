require("dotenv").config();
const express = require("express");
const cron = require("node-cron"); // npm install node-cron 필요
const { fetchRecentLogs } = require("./lib/loki");
const { analyzeAlert } = require("./lib/gemini");
const { sendToSlack, buildBaseBlocks, buildActionButtons, buildTeamProgressSection } = require("./lib/slack");
const { evaluateAlert } = require("./config/actions");
const { runAction } = require("./lib/executor");
const { verifySlackSignature } = require("./lib/slackVerify");
const approvalTracker = require("./lib/approvalTracker");
const { notifyManagerOnResolve, sendDailyDigest, DIGEST_CRON } = require("./lib/notifyManager");

const app = express();

const PORT = process.env.PORT || 4000;

/**
 * Grafana Alerting > Contact points 에서
 * Integration: Webhook, URL: http://<이-서버>:4000/webhook/grafana 로 등록하면
 * 알럿이 firing/resolved 될 때마다 이 엔드포인트로 POST가 온다.
 */
app.post("/webhook/grafana", express.json(), async (req, res) => {
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

  // SINGLE_APPROVAL / TEAM_APPROVAL 인 경우, 나중에 Slack 버튼 클릭이 왔을 때
  // "무엇에 대한 승인인지" 추적할 고유 ID를 만들어둔다.
  const instanceId = `${alertName}::${Date.now()}`;

  // 1. 개발팀 Slack 통보가 먼저
  await sendToSlack({ alertName, status, value, labels, analysis, policy, executionResult, instanceId });
  console.log(`[webhook] 처리 완료: ${alertName} (경로: ${policy.route}, instanceId: ${instanceId})`);

  // 2. 자동 실행(AUTO_EXECUTE)은 이 시점에 이미 해결/실패가 결정된 상태이므로,
  //    Slack 통보 다음 순서로 매니저 알림을 보낸다.
  //    SINGLE_APPROVAL/TEAM_APPROVAL은 아직 결과가 없으므로 여기서는 아무것도 보내지 않고,
  //    handleSingleApproval()/handleTeamApproval()에서 결과가 나온 뒤에 보낸다.
  if (executionResult) {
    await notifyManagerOnResolve({ alertName, policy, result: executionResult });
  }
}

function formatValues(values) {
  if (!values) return null;
  return Object.entries(values)
    .map(([k, v]) => `${k}=${v}`)
    .join(", ");
}

app.get("/health", (req, res) => res.json({ status: "ok" }));

/**
 * Slack Interactivity & Shortcuts 의 Request URL로 등록하는 엔드포인트.
 * "승인/거부" 버튼 클릭 시 Slack이 여기로 POST를 보낸다.
 *
 * express.raw()로 원본 바이트를 그대로 받아서 서명 검증부터 먼저 하고,
 * 검증 통과 후에 querystring으로 직접 파싱한다.
 * (express.urlencoded의 verify 콜백 방식이 이 환경에서 원본 바이트를
 *  온전히 못 넘겨주는 문제가 있어서, 더 안전한 이 방식으로 처리한다.)
 */
app.post("/slack/interactions", express.raw({ type: "application/x-www-form-urlencoded" }), async (req, res) => {
  const rawBody = req.body.toString("utf8");
  const isValid = verifySlackSignature(req, rawBody);
  if (!isValid) {
    console.error("[slack-interactions] 서명 검증 실패, 요청 거부");
    return res.status(401).send("invalid signature");
  }

  res.status(200).send("");

  const querystring = require("querystring");
  const parsedBody = querystring.parse(rawBody);

  let payload;
  try {
    payload = JSON.parse(parsedBody.payload);
  } catch (err) {
    console.error("[slack-interactions] payload 파싱 실패:", err.message);
    return;
  }

  const action = payload.actions?.[0];
  if (!action) {
    console.error("[slack-interactions] action 정보 없음");
    return;
  }

  let actionData;
  try {
    actionData = JSON.parse(action.value);
  } catch (err) {
    console.error("[slack-interactions] action value 파싱 실패:", err.message);
    return;
  }

  const approver = payload.user?.username || payload.user?.id || "알수없음";
  const { alertName, value, instanceId } = actionData;
  const responseUrl = payload.response_url;

  // 신뢰 원본은 항상 서버: 클라이언트(Slack 버튼)가 보낸 scriptId/action 텍스트를
  // 그대로 믿지 않고, 클릭 시점에 정책을 다시 계산한다.
  const policy = evaluateAlert(alertName);

  if (approvalTracker.isFinalized(instanceId)) {
    console.log(`[slack-interactions] 이미 처리 완료된 건에 대한 클릭 무시: ${instanceId}`);
    return;
  }

  if (policy.route === "SINGLE_APPROVAL") {
    await handleSingleApproval({ action, approver, alertName, value, policy, responseUrl });
    approvalTracker.markFinalized(instanceId);
  } else if (policy.route === "TEAM_APPROVAL") {
    await handleTeamApproval({ action, approver, alertName, value, policy, instanceId, responseUrl });
  } else {
    console.error(`[slack-interactions] 승인 버튼이 필요 없는 등급(${policy.route})인데 클릭됨, 무시`);
  }
});

async function handleSingleApproval({ action, approver, alertName, value, policy, responseUrl }) {
  if (action.action_id === "approve_action") {
    console.log(`[slack-interactions] ${approver}님이 승인함: ${alertName} (${policy.scriptId})`);
    const result = policy.scriptId
      ? await runAction(policy.scriptId, { alertName, value })
      : { success: false, message: "실행 가능한 조치가 등록되어 있지 않습니다." };

    // 1. 개발팀 Slack 갱신이 먼저
    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `✅ *${approver}님이 승인했습니다*\n조치: ${policy.action}\n실행 결과: ${result.message}`,
    });

    // 2. 그 다음 매니저 알림 (등급이 낮으면 다이제스트로 쌓임)
    await notifyManagerOnResolve({ alertName, policy, result });
  } else if (action.action_id === "reject_action") {
    console.log(`[slack-interactions] ${approver}님이 거부함: ${alertName}`);

    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `❌ *${approver}님이 거부했습니다*\n조치: ${policy.action}\n실행되지 않았습니다.`,
    });

    await notifyManagerOnResolve({
      alertName,
      policy,
      result: { success: false, message: `${approver}님이 거부하여 실행되지 않음` },
    });
  }
}

async function handleTeamApproval({ action, approver, alertName, value, policy, instanceId, responseUrl }) {
  if (action.action_id === "reject_action") {
    // 한 명이라도 거부하면 즉시 중단 (보수적으로 설계: 만장일치 승인 원칙)
    approvalTracker.addRejection(instanceId, approver);
    approvalTracker.markFinalized(instanceId);
    console.log(`[slack-interactions] ${approver}님이 거부함(고위험, 즉시 중단): ${alertName}`);

    // 1. 개발팀 Slack 갱신이 먼저
    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `❌ *${approver}님이 거부하여 조치가 중단되었습니다*\n조치: ${policy.action}`,
    });

    // 2. 그 다음 매니저 알림: HIGH 등급이라 거부되어도 즉시 통보 (숨기지 않음)
    await notifyManagerOnResolve({
      alertName,
      policy,
      result: { success: false, message: `${approver}님이 거부하여 조치가 중단됨` },
    });
    return;
  }

  if (action.action_id === "approve_action") {
    if (approvalTracker.hasApproved(instanceId, approver)) {
      console.log(`[slack-interactions] ${approver}님은 이미 승인함, 중복 무시: ${alertName}`);
      return;
    }

    const approvedCount = approvalTracker.addApproval(instanceId, approver);
    const approvers = approvalTracker.getApprovers(instanceId);
    console.log(
      `[slack-interactions] ${approver}님이 승인함 (${approvedCount}/${policy.requiredApprovals}): ${alertName}`
    );

    if (approvedCount >= policy.requiredApprovals) {
      approvalTracker.markFinalized(instanceId);
      const result = policy.scriptId
        ? await runAction(policy.scriptId, { alertName, value })
        : { success: true, message: "이 알럿은 자동 실행 조치가 없습니다. 팀 승인 완료 후 수동 대응이 필요합니다." };

      // 1. 개발팀 Slack 갱신이 먼저
      await respondToSlack(responseUrl, {
        replace_original: true,
        text:
          `✅ *팀 승인 완료* (${approvers.join(", ")})\n` +
          `조치: ${policy.action}\n` +
          `실행 결과: ${result.message}`,
      });

      // 2. 그 다음 매니저 알림: 팀 승인 완료 후 최종 처리 결과 통보
      await notifyManagerOnResolve({ alertName, policy, result });
    } else {
      // 아직 정족수 미달 — 진행 현황만 갱신하고 버튼은 그대로 유지해서
      // 다른 팀원이 계속 승인할 수 있게 한다.
      const analysis = { 발생: "(승인 진행 중)", why: "-", how: policy.action };
      const baseBlocks = buildBaseBlocks({
        alertName,
        status: "firing",
        value,
        labels: {},
        analysis,
        policy,
      });
      baseBlocks.push(
        buildTeamProgressSection({ approvedCount, requiredApprovals: policy.requiredApprovals }),
        buildActionButtons({ alertName, value, instanceId })
      );
      await respondToSlack(responseUrl, { replace_original: true, blocks: baseBlocks });
    }
  }
}

/**
 * Slack이 보내준 response_url로 결과를 다시 전송해서 원본 메시지를 갱신한다.
 * (별도 Bot Token 없이도 동작 — response_url은 30분간, 최대 5회까지 사용 가능)
 */
async function respondToSlack(responseUrl, body) {
  if (!responseUrl) return;
  try {
    const res = await fetch(responseUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      console.error(`[slack-interactions] response_url 응답 실패: HTTP ${res.status}`);
    }
  } catch (err) {
    console.error("[slack-interactions] response_url 전송 중 오류:", err.message);
  }
}

// 매니저용 다이제스트: LOW/MEDIUM 등급으로 쌓인 알림을 하루 한 번 요약해서 전송
cron.schedule(DIGEST_CRON, sendDailyDigest);

// ⚠️ 테스트 전용 - 다이제스트 즉시 발송 확인용, 검증 끝나면 삭제할 것
app.get("/debug/send-digest-now", async (req, res) => {
  await sendDailyDigest();
  res.json({ ok: true, message: "다이제스트 발송 시도 완료, 콘솔/Sendbird 확인" });
});

app.listen(PORT, () => {
  console.log(`AIOps 중계서버가 http://localhost:${PORT} 에서 실행 중`);
  console.log(`Grafana webhook 등록 주소: http://<이-서버-IP>:${PORT}/webhook/grafana`);
});
