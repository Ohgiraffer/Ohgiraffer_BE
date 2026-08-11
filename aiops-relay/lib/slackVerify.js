const crypto = require("crypto");

const SIGNING_SECRET = process.env.SLACK_SIGNING_SECRET;

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

  const fiveMinutesAgo = Math.floor(Date.now() / 1000) - 60 * 5;
  if (Number(timestamp) < fiveMinutesAgo) {
    console.error("[slack-verify] 타임스탬프가 너무 오래됨 (재전송 공격 의심)");
    return false;
  }

  const baseString = `v0:${timestamp}:${rawBody}`;
  const computedSignature =
    "v0=" + crypto.createHmac("sha256", SIGNING_SECRET).update(baseString).digest("hex");

  try {
    return crypto.timingSafeEqual(Buffer.from(computedSignature), Buffer.from(signature));
  } catch {
    return false;
  }
}

module.exports = { verifySlackSignature };
