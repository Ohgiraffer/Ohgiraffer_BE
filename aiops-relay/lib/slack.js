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
 * 알럿 메시지의 공통 블록(헤더/현재값/AI분석/등급판정)을 만든다.
 * 초기 전송(sendToSlack)과, 승인 진행 중 메시지 갱신(server.js) 양쪽에서
 * 같은 형태를 재사용해서 메시지가 매번 다르게 보이지 않게 한다.
 */
function buildBaseBlocks({ alertName, status, value, labels, analysis, policy }) {
  const emoji = STATUS_EMOJI[status] || "ℹ️";
  const labelText = Object.entries(labels || {})
    .map(([k, v]) => `\`${k}=${v}\``)
    .join(" ");

  return [
    {
      type: "header",
      text: { type: "plain_text", text: `${emoji} [${status.toUpperCase()}] ${alertName}` },
    },
    {
      type: "section",
      fields: [
        { type: "mrkdwn", text: `*현재 값*\n${value}` },
        { type: "mrkdwn", text: `*라벨*\n${labelText || "-"}` },
      ],
    },
    { type: "divider" },
    { type: "section", text: { type: "mrkdwn", text: `*🔍 발생*\n${analysis.발생}` } },
    { type: "section", text: { type: "mrkdwn", text: `*❓ Why (원인 추정)*\n${analysis.why}` } },
    { type: "section", text: { type: "mrkdwn", text: `*🛠 How (권장 조치)*\n${analysis.how}` } },
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
  ];
}

/**
 * 승인/거부 버튼 블록. value에는 alertName, value, instanceId만 담는다.
 * scriptId나 action 텍스트는 절대 클라이언트(Slack) 쪽에 신뢰 원본으로 두지 않고,
 * 클릭 시점에 서버가 evaluateAlert()로 다시 계산해서 쓴다 (신뢰 원본은 항상 서버 코드).
 */
function buildActionButtons({ alertName, value, instanceId }) {
  const actionPayload = JSON.stringify({ alertName, value, instanceId });
  return {
    type: "actions",
    elements: [
      {
        type: "button",
        text: { type: "plain_text", text: "✅ 승인" },
        style: "primary",
        action_id: "approve_action",
        value: actionPayload,
      },
      {
        type: "button",
        text: { type: "plain_text", text: "❌ 거부" },
        style: "danger",
        action_id: "reject_action",
        value: actionPayload,
      },
    ],
  };
}

/**
 * 고위험(TEAM_APPROVAL) 알럿의 "N/M명 승인" 진행 현황 섹션.
 */
function buildTeamProgressSection({ approvedCount, requiredApprovals }) {
  return {
    type: "section",
    text: { type: "mrkdwn", text: `*승인 현황*: ${approvedCount}/${requiredApprovals}명 승인` },
  };
}

/**
 * 최초 알럿 발생 시 Slack으로 메시지를 보낸다.
 */
async function sendToSlack({ alertName, status, value, labels, analysis, policy, executionResult, instanceId }) {
  const isPlaceholder =
    !SLACK_WEBHOOK_URL || /[^\x00-\x7F]/.test(SLACK_WEBHOOK_URL) || SLACK_WEBHOOK_URL.trim() === "";

  if (isPlaceholder) {
    console.error(
      "[slack] SLACK_WEBHOOK_URL이 설정되지 않았거나 .env.example 플레이스홀더가 남아있어 전송을 건너뜁니다."
    );
    return;
  }

  const blocks = buildBaseBlocks({ alertName, status, value, labels, analysis, policy });

  if (policy && policy.route === "SINGLE_APPROVAL") {
    blocks.push(buildActionButtons({ alertName, value, instanceId }));
  } else if (policy && policy.route === "TEAM_APPROVAL") {
    blocks.push(
      buildTeamProgressSection({ approvedCount: 0, requiredApprovals: policy.requiredApprovals }),
      buildActionButtons({ alertName, value, instanceId })
    );
  }

  if (executionResult) {
    blocks.push({
      type: "section",
      text: {
        type: "mrkdwn",
        text: `*${executionResult.success ? "✅ 자동 실행 완료" : "⚠️ 자동 실행 실패"}*\n${executionResult.message}`,
      },
    });
  }

  try {
    const res = await fetch(SLACK_WEBHOOK_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ blocks }),
    });
    if (!res.ok) {
      console.error(`[slack] 전송 실패: HTTP ${res.status}`);
    }
  } catch (err) {
    console.error("[slack] 전송 중 예외 발생:", err.message);
  }
}

module.exports = { sendToSlack, buildBaseBlocks, buildActionButtons, buildTeamProgressSection };
