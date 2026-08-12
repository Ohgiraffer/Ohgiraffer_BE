const crypto = require("crypto");

const SIGNING_SECRET = process.env.SLACK_SIGNING_SECRET;

/**
 * Slack의 요청 서명을 검증한다.
 * https://api.slack.com/authentication/verifying-requests-from-slack
 *
 * Slack이 보낸 요청이 아니면(서명이 안 맞으면) 누구나 우리 서버에
 * "승인됨"이라고 가짜 요청을 보낼 수 있으므로, 이 검증 없이는
 * 절대 버튼 클릭 이벤트를 신뢰해서 처리하면 안 된다.
 */
function verifySlackSignature(req, rawBody) {
  if (!SIGNING_SECRET || /[^\x00-\x7F]/.test(SIGNING_SECRET)) {
    console.error(
      "[slack-verify] SLACK_SIGNING_SECRET이 설정되지 않았습니다. 검증을 건너뛸 수 없어 요청을 거부합니다."
    );
    return false;
  }

  const timestamp = req.headers["x-slack-request-timestamp"];
  const signature = req.headers["x-slack-signature"];

  if (!timestamp || !signature) {
    return false;
  }

  // 재전송 공격(replay attack) 방지: 5분 이상 지난 요청은 거부
  const fiveMinutesAgo = Math.floor(Date.now() / 1000) - 60 * 5;
  if (Number(timestamp) < fiveMinutesAgo) {
    console.error("[slack-verify] 타임스탬프가 너무 오래됨 (재전송 공격 의심)");
    return false;
  }

  const baseString = `v0:${timestamp}:${rawBody}`;
  const computedSignature =
    "v0=" + crypto.createHmac("sha256", SIGNING_SECRET).update(baseString).digest("hex");

  // 타이밍 공격 방지를 위해 timingSafeEqual 사용
  try {
    return crypto.timingSafeEqual(Buffer.from(computedSignature), Buffer.from(signature));
  } catch {
    return false; // 길이가 다르면 timingSafeEqual이 예외를 던짐
  }
}

module.exports = { verifySlackSignature };
