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
const { logAgentReasoning } = require("./lib/reasoningLogClient");
const { validateSnsMessage } = require("./lib/snsVerify");

const app = express();

const PORT = process.env.PORT || 4000;

// SINGLE_APPROVAL/TEAM_APPROVAL 경로는 Slack 버튼 클릭이라는 별도 요청으로 들어오기 때문에
// handleSingleAlert()에서 만든 원본 analysis를 잃어버린다. instanceId를 키로 임시 보관했다가
// 승인/거부가 최종 확정되는 시점에 꺼내 쓰고 즉시 지운다 (승인 안 되고 방치된 건은 남을 수 있음 -
// approvalTracker와 마찬가지로 프로세스 재시작 시 유실되는 휘발성 저장소).
const analysisStore = new Map();

// SNS는 최소 1회 전송을 보장하므로 같은 알림이 중복으로 올 수 있다. MessageId 기준으로
// 이미 처리한 건은 다시 실행하지 않는다 (analysisStore/approvalTracker와 마찬가지로
// 프로세스 재시작 시 유실되는 휘발성 저장소 - 이 정도 volatility는 기존 코드베이스 전례를 따름).
const processedSnsMessageIds = new Map(); // messageId -> 처리 시각(ms)
const SNS_MESSAGE_ID_TTL_MS = 60 * 60 * 1000; // 1시간 지나면 정리 대상

function isDuplicateSnsMessage(messageId) {
  if (!messageId) return false; // MessageId가 없는 비정상 메시지는 중복 판단 자체를 건너뜀

  pruneExpiredSnsMessageIds();

  if (processedSnsMessageIds.has(messageId)) {
    return true;
  }

  processedSnsMessageIds.set(messageId, Date.now());
  return false;
}

function pruneExpiredSnsMessageIds() {
  const now = Date.now();
  for (const [messageId, processedAt] of processedSnsMessageIds) {
    if (now - processedAt > SNS_MESSAGE_ID_TTL_MS) {
      processedSnsMessageIds.delete(messageId);
    }
  }
}

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

  // SINGLE_APPROVAL / TEAM_APPROVAL 인 경우, 나중에 Slack 버튼 클릭이 왔을 때
  // "무엇에 대한 승인인지" 추적할 고유 ID를 만들어둔다. (AUTO_EXECUTE 로깅에서도 재사용)
  const instanceId = `${alertName}::${Date.now()}`;
  analysisStore.set(instanceId, analysis);

  let executionResult = null;
  if (policy.route === "AUTO_EXECUTE" && policy.autoExecutable) {
    console.log(`[webhook] 저위험 판정 → 자동 실행 시도: ${policy.scriptId}`);
    const startedAt = Date.now();
    executionResult = await runAction(policy.scriptId, { alertName, value, labels });
    console.log("[webhook] 자동 실행 결과:", executionResult);

    logAgentReasoning({
      sessionId: alertName,
      turnId: instanceId,
      functionName: policy.scriptId,
      analysis,
      success: executionResult.success,
      latencyMs: Date.now() - startedAt,
    });
  }

  // 1. 개발팀 Slack 통보가 먼저
  await sendToSlack({ alertName, status, value, labels, analysis, policy, executionResult, instanceId });
  console.log(`[webhook] 처리 완료: ${alertName} (경로: ${policy.route}, instanceId: ${instanceId})`);

  // 2. 자동 실행(AUTO_EXECUTE)은 이 시점에 이미 해결/실패가 결정된 상태이므로,
  //    Slack 통보 다음 순서로 매니저 알림을 보낸다.
  //    SINGLE_APPROVAL/TEAM_APPROVAL은 아직 결과가 없으므로 여기서는 아무것도 보내지 않고,
  //    handleSingleApproval()/handleTeamApproval()에서 결과가 나온 뒤에 보낸다.
  if (executionResult) {
    analysisStore.delete(instanceId); // 이 경로는 여기서 바로 최종 확정되므로 저장해둘 필요 없음
    await notifyManagerOnResolve({ alertName, policy, analysis, result: executionResult });
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
 * CloudWatch Alarm -> SNS -> 이 엔드포인트로 들어오는 웹훅.
 * 이 라우트 자체의 경로는 /webhook/cloudwatch-alarm (Express 기준).
 * SNS Topic의 HTTPS 구독 엔드포인트로 등록할 값은 nginx가 /aiops/ 경로를 이 서버(4000번 포트)로
 * 리버스 프록시하고 있는 걸 반영한 외부 접근 주소다: https://<모니터링서버 도메인>/aiops/webhook/cloudwatch-alarm
 * (nginx 프록시 설정이 바뀌면 이 프리픽스도 같이 확인할 것)
 *
 * SNS는 Content-Type을 text/plain으로 보내면서 JSON 문자열을 담아 보내는 경우가 많아
 * express.json() 대신 express.text()로 원본을 받아 직접 파싱한다.
 *
 * 서명 검증(validateSnsMessage)을 반드시 통과해야 처리한다 - 이게 없으면 외부에서
 * 이 URL만 알아도 가짜 알람을 흉내 내어 AUTO_EXECUTE 화이트리스트 액션을 실행시킬 수 있다.
 * 다만 서명 검증은 "진짜 AWS SNS가 보낸 메시지"라는 것만 증명하지 "우리가 만든 그 토픽에서
 * 온 메시지"라는 것까진 보장 안 한다 - 그래서 TopicArn까지 허용 목록으로 한 번 더 검증한다.
 */
app.post("/webhook/cloudwatch-alarm", express.text({ type: "*/*" }), async (req, res) => {
  let message;
  try {
    message = JSON.parse(req.body);
  } catch (err) {
    console.error("[cloudwatch-alarm] JSON 파싱 실패:", err.message);
    return res.status(400).send("invalid json");
  }

  try {
    await validateSnsMessage(message);
  } catch (err) {
    console.error("[cloudwatch-alarm] SNS 서명 검증 실패:", err.message);
    return res.status(401).send("invalid signature");
  }

  // 서명 검증만으론 "우리 토픽에서 온 메시지"라는 게 증명 안 됨 - 공격자가 자기 SNS 토픽을
  // 만들어서 이 URL을 구독시키고 알럿명을 흉내 낸 가짜 알림을 보내는 걸 여기서 차단한다.
  const allowedTopicArn = process.env.SNS_ALLOWED_TOPIC_ARN;
  if (allowedTopicArn && message.TopicArn !== allowedTopicArn) {
    console.error(`[cloudwatch-alarm] 허용되지 않은 TopicArn, 거부: ${message.TopicArn}`);
    return res.status(403).send("topic not allowed");
  }
  if (!allowedTopicArn) {
    console.error("[cloudwatch-alarm] SNS_ALLOWED_TOPIC_ARN 미설정 - TopicArn 검증 없이 통과시키는 중 (배포 전 반드시 설정할 것)");
  }

  // 라우팅 판단은 반드시 서명 검증 대상인 body의 Type 필드로만 한다.
  // req.header()는 SNS 서명 검증 범위 밖이라 위조 가능하므로 신뢰하지 않는다.
  const messageType = message.Type;

  if (messageType === "SubscriptionConfirmation") {
    res.status(200).send("ok");
    try {
      await fetch(message.SubscribeURL);
      console.log("[cloudwatch-alarm] SNS 구독 확인 완료");
    } catch (err) {
      console.error("[cloudwatch-alarm] SNS 구독 확인 요청 실패:", err.message);
    }
    return;
  }

  if (messageType === "Notification") {
    // SNS는 최소 1회 전송을 보장하므로 같은 알림이 중복으로 올 수 있다.
    // MessageId 기준으로 이미 처리한 건은 다시 실행하지 않는다 (AUTO_EXECUTE 중복 실행 방지).
    if (isDuplicateSnsMessage(message.MessageId)) {
      console.log(`[cloudwatch-alarm] 이미 처리한 MessageId, 무시: ${message.MessageId}`);
      return res.status(200).send("duplicate");
    }

    res.status(200).send("ok");

    let alarmData;
    try {
      alarmData = JSON.parse(message.Message);
    } catch (err) {
      console.error("[cloudwatch-alarm] Message 파싱 실패:", err.message);
      return;
    }

    try {
      await handleCloudWatchAlarm(alarmData);
    } catch (err) {
      console.error("[cloudwatch-alarm] 알람 처리 중 오류:", err);
    }
    return;
  }

  res.status(200).send("ok");
});

/**
 * CloudWatch Alarm 상태 변경 데이터를, 기존 handleSingleAlert()가 기대하는
 * Grafana 알럿 형태로 변환해서 동일한 Slack/승인/매니저 알림 파이프라인을 그대로 재사용한다.
 * config/actions.js의 ALERT_POLICY에 등록 안 된 알람명은 DEFAULT_POLICY(HIGH, 팀 전체 승인)로
 * 안전하게 처리되므로, 새 CloudWatch 알람을 추가할 때마다 정책을 반드시 등록할 필요는 없다.
 */
async function handleCloudWatchAlarm(alarmData) {
  const alertName = alarmData.AlarmName || "이름없는CloudWatch알람";
  const newState = alarmData.NewStateValue; // "ALARM" | "OK" | "INSUFFICIENT_DATA"

  if (newState === "INSUFFICIENT_DATA") {
    console.log(`[cloudwatch-alarm] 데이터 부족 상태, 무시: ${alertName}`);
    return;
  }

  const status = newState === "ALARM" ? "firing" : "resolved";
  const reason = alarmData.NewStateReason || "-";

  console.log(`[cloudwatch-alarm] 처리 시작: ${alertName} (${status})`);

  await handleSingleAlert({
    status,
    labels: { alertname: alertName },
    values: { reason },
  });
}

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
    await handleSingleApproval({ action, approver, alertName, value, policy, instanceId, responseUrl });
    approvalTracker.markFinalized(instanceId);
  } else if (policy.route === "TEAM_APPROVAL") {
    await handleTeamApproval({ action, approver, alertName, value, policy, instanceId, responseUrl });
  } else {
    console.error(`[slack-interactions] 승인 버튼이 필요 없는 등급(${policy.route})인데 클릭됨, 무시`);
  }
});

async function handleSingleApproval({ action, approver, alertName, value, policy, instanceId, responseUrl }) {
  // 이 건은 여기서 최종 확정되므로 저장된 analysis를 꺼내고 바로 지운다.
  const analysis = analysisStore.get(instanceId);
  analysisStore.delete(instanceId);

  if (action.action_id === "approve_action") {
    console.log(`[slack-interactions] ${approver}님이 승인함: ${alertName} (${policy.scriptId})`);
    const startedAt = Date.now();
    const result = policy.scriptId
        ? await runAction(policy.scriptId, { alertName, value })
        : { success: false, message: "실행 가능한 조치가 등록되어 있지 않습니다." };

    if (policy.scriptId) {
      logAgentReasoning({
        sessionId: alertName,
        turnId: instanceId,
        functionName: policy.scriptId,
        analysis,
        success: result.success,
        latencyMs: Date.now() - startedAt,
      });
    }

    // 1. 개발팀 Slack 갱신이 먼저
    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `✅ *${approver}님이 승인했습니다*\n조치: ${policy.action}\n실행 결과: ${result.message}`,
    });

    // 2. 그 다음 매니저 알림 (등급이 낮으면 다이제스트로 쌓임)
    await notifyManagerOnResolve({ alertName, policy, analysis, result });
  } else if (action.action_id === "reject_action") {
    console.log(`[slack-interactions] ${approver}님이 거부함: ${alertName}`);

    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `❌ *${approver}님이 거부했습니다*\n조치: ${policy.action}\n실행되지 않았습니다.`,
    });

    await notifyManagerOnResolve({
      alertName,
      policy,
      analysis,
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

    const rejectedAnalysis = analysisStore.get(instanceId);
    analysisStore.delete(instanceId);

    // 1. 개발팀 Slack 갱신이 먼저
    await respondToSlack(responseUrl, {
      replace_original: true,
      text: `❌ *${approver}님이 거부하여 조치가 중단되었습니다*\n조치: ${policy.action}`,
    });

    // 2. 그 다음 매니저 알림: HIGH 등급이라 거부되어도 즉시 통보 (숨기지 않음)
    await notifyManagerOnResolve({
      alertName,
      policy,
      analysis: rejectedAnalysis,
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
      const finalizedAnalysis = analysisStore.get(instanceId);
      analysisStore.delete(instanceId);
      const startedAt = Date.now();
      const result = policy.scriptId
          ? await runAction(policy.scriptId, { alertName, value })
          : {
            success: false,
            status: "MANUAL_REQUIRED",
            message: "이 알럿은 자동 실행 조치가 없습니다. 팀 승인 완료 후 수동 대응이 필요합니다.",
          };

      if (policy.scriptId) {
        logAgentReasoning({
          sessionId: alertName,
          turnId: instanceId,
          functionName: policy.scriptId,
          analysis: finalizedAnalysis,
          success: result.success,
          latencyMs: Date.now() - startedAt,
        });
      }

      // 1. 개발팀 Slack 갱신이 먼저
      await respondToSlack(responseUrl, {
        replace_original: true,
        text:
            `✅ *팀 승인 완료* (${approvers.join(", ")})\n` +
            `조치: ${policy.action}\n` +
            `실행 결과: ${result.message}`,
      });

      // 2. 그 다음 매니저 알림: 팀 승인 완료 후 최종 처리 결과 통보
      await notifyManagerOnResolve({ alertName, policy, analysis: finalizedAnalysis, result });
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


app.listen(PORT, () => {
  console.log(`AIOps 중계서버가 http://localhost:${PORT} 에서 실행 중`);
  console.log(`Grafana webhook 등록 주소: http://<이-서버-IP>:${PORT}/webhook/grafana`);
});