const SLACK_WEBHOOK_URL = process.env.SLACK_WEBHOOK_URL;

const STATUS_EMOJI = {
  firing: "🚨",
  resolved: "✅",
};

const ROUTE_LABEL = {
  AUTO_EXECUTE: "🟢 저위험 · 자동 실행 대상",
  SINGLE_APPROVAL: "🟡 중위험 · 1인 승인 필요",
  TEAM_APPROVAL: "🔴 고위험 · 팀 전체 승인 필요",
};

/**
 * AI 분석 결과 + 위험 등급 판정 결과를 Slack 메시지(블록 포맷)로 변환해서 전송한다.
 * policy는 config/actions.js의 evaluateAlert() 반환값 (없으면 등급 섹션 생략).
 */
async function sendToSlack({ alertName, status, value, labels, analysis, policy, executionResult }) {
  const isPlaceholder =
    !SLACK_WEBHOOK_URL || /[^\x00-\x7F]/.test(SLACK_WEBHOOK_URL) || SLACK_WEBHOOK_URL.trim() === "";

  if (isPlaceholder) {
    console.error(
      "[slack] SLACK_WEBHOOK_URL이 설정되지 않았거나 .env.example 플레이스홀더가 남아있어 전송을 건너뜁니다."
    );
    return;
  }

  const emoji = STATUS_EMOJI[status] || "ℹ️";
  const labelText = Object.entries(labels || {})
    .map(([k, v]) => `\`${k}=${v}\``)
    .join(" ");

  const payload = {
    blocks: [
      {
        type: "header",
        text: {
          type: "plain_text",
          text: `${emoji} [${status.toUpperCase()}] ${alertName}`,
        },
      },
      {
        type: "section",
        fields: [
          { type: "mrkdwn", text: `*현재 값*\n${value}` },
          { type: "mrkdwn", text: `*라벨*\n${labelText || "-"}` },
        ],
      },
      { type: "divider" },
      {
        type: "section",
        text: { type: "mrkdwn", text: `*🔍 발생*\n${analysis.발생}` },
      },
      {
        type: "section",
        text: { type: "mrkdwn", text: `*❓ Why (원인 추정)*\n${analysis.why}` },
      },
      {
        type: "section",
        text: { type: "mrkdwn", text: `*🛠 How (권장 조치)*\n${analysis.how}` },
      },
      ...(policy
        ? [
            { type: "divider" },
            {
              type: "section",
              text: {
                type: "mrkdwn",
                text: `*등급 판정*\n${ROUTE_LABEL[policy.route] || policy.route}\n권장 조치: ${policy.action}`,
              },
            },
          ]
        : []),
      ...(executionResult
        ? [
            {
              type: "section",
              text: {
                type: "mrkdwn",
                text: `*${executionResult.success ? "✅ 자동 실행 완료" : "⚠️ 자동 실행 실패"}*\n${
                  executionResult.message
                }`,
              },
            },
          ]
        : []),
    ],
  };

  try {
    const res = await fetch(SLACK_WEBHOOK_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      console.error(`[slack] 전송 실패: HTTP ${res.status}`);
    }
  } catch (err) {
    console.error("[slack] 전송 중 예외 발생:", err.message);
  }
}

module.exports = { sendToSlack };
