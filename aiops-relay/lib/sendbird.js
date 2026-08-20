/**
 * lib/sendbird.js
 *
 * aiops-relay는 로그인한 유저가 아니라 서버 프로세스이므로,
 * 프론트가 쓰는 세션 토큰 흐름(SendbirdSessionTokenResponse)을 타지 않는다.
 * 대신 Sendbird Platform API를 마스터 API 토큰으로 직접 호출한다.
 *
 * https://api-{APP_ID}.sendbird.com/v3/...
 * Header: Api-Token: {마스터 API 토큰}
 *
 * 이 토큰은 백엔드가 세션 토큰을 발급할 때 쓰는 것과 동일한 값이다.
 * 유출 시 영향 범위가 크므로, 가능하면 Sendbird 콘솔에서 aiops-relay 전용으로
 * 읽기/쓰기 권한만 있는 별도 토큰을 발급받아 SENDBIRD_API_TOKEN에 넣는 걸 권장.
 */

const APP_ID = process.env.SENDBIRD_APP_ID;
const API_TOKEN = process.env.SENDBIRD_API_TOKEN;
const BOT_USER_ID = process.env.SENDBIRD_BOT_USER_ID || "aiops-bot";

const BASE_URL = `https://api-${APP_ID}.sendbird.com/v3`;

function isPlaceholder(v) {
  return !v || /[^\x00-\x7F]/.test(v) || v.trim() === "";
}

function assertConfigured() {
  if (isPlaceholder(APP_ID) || isPlaceholder(API_TOKEN)) {
    throw new Error(
      "[sendbird] SENDBIRD_APP_ID 또는 SENDBIRD_API_TOKEN이 설정되지 않았습니다. .env를 확인하세요."
    );
  }
}

async function callSendbird(method, path, body) {
  assertConfigured();

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: {
      "Api-Token": API_TOKEN,
      "Content-Type": "application/json",
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    const err = new Error(`[sendbird] ${method} ${path} 실패: HTTP ${res.status} - ${JSON.stringify(data)}`);
    err.status = res.status;
    err.body = data;
    throw err;
  }

  return data;
}

/**
 * aiops-bot 유저가 이미 있으면 그대로 두고, 없으면 새로 만든다.
 * 셋업 스크립트(scripts/setup-manager-channel.js)에서 최초 1회 호출하면 되고,
 * 매 알림마다 호출할 필요는 없다.
 */
async function ensureBotUser() {
  try {
    await callSendbird("GET", `/users/${BOT_USER_ID}`);
    console.log(`[sendbird] 봇 유저 이미 존재함: ${BOT_USER_ID}`);
  } catch (err) {
    if (err.status !== 400 && err.status !== 404) throw err;
    console.log(`[sendbird] 봇 유저 없음, 새로 생성: ${BOT_USER_ID}`);
    await callSendbird("POST", "/users", {
      user_id: BOT_USER_ID,
      nickname: "AIOps Bot",
      profile_url: "", // Sendbird가 이 필드 자체는 필수로 요구함 (값은 비어도 됨)
    });
  }
  return BOT_USER_ID;
}

/**
 * 매니저들만 있는 그룹 채널을 새로 만든다.
 * 셋업 스크립트에서 최초 1회만 호출하고, 반환된 channel_url을
 * .env의 MANAGER_CHANNEL_URL에 고정해서 재사용한다.
 * (매번 새로 만들면 매니저 쪽에 알림방이 계속 늘어나므로 절대 매 알림마다 호출하지 말 것)
 */
async function createManagerChannel(managerUserIds, channelName = "AIOps 알림") {
  const userIds = [BOT_USER_ID, ...managerUserIds];
  const data = await callSendbird("POST", "/group_channels", {
    user_ids: userIds,
    name: channelName,
    is_distinct: false, // 같은 멤버 조합이라도 항상 새 채널로 (재실행 시 중복 채널 생성 방지는 셋업 스크립트에서 별도 처리)
  });
  return data.channel_url;
}

/**
 * 고정된 매니저 채널에 봇 명의로 메시지를 보낸다.
 * notifyManager.js가 실제로 호출하는 유일한 함수.
 */
async function sendMessageToManagerChannel(text) {
  const channelUrl = process.env.MANAGER_CHANNEL_URL;
  if (isPlaceholder(channelUrl)) {
    console.error("[sendbird] MANAGER_CHANNEL_URL 미설정 - 전송 스킵 (.env 확인 필요)");
    return;
  }

  try {
    await callSendbird("POST", `/group_channels/${encodeURIComponent(channelUrl)}/messages`, {
      message_type: "MESG",
      user_id: BOT_USER_ID,
      message: text,
    });
  } catch (err) {
    // Slack과 마찬가지로 알림 전송 실패가 전체 파이프라인을 죽이면 안 됨
    console.error("[sendbird] 매니저 채널 전송 실패:", err.message);
  }
}

module.exports = {
  ensureBotUser,
  createManagerChannel,
  sendMessageToManagerChannel,
};
