/**
 * check-sendbird-users.js
 * 매니저(userId 1~5)가 Sendbird에 실제로 어떤 user_id 형태로 등록돼 있는지 확인.
 * 읽기 전용 GET이라 아무 것도 생성/변경하지 않는다.
 *
 * 실행: node check-sendbird-users.js
 * (aiops-relay/.env에 SENDBIRD_APP_ID, SENDBIRD_API_TOKEN이 이미 있어야 함)
 */
require("dotenv").config();

const APP_ID = process.env.SENDBIRD_APP_ID;
const API_TOKEN = process.env.SENDBIRD_API_TOKEN;

async function main() {
  const url = `https://api-${APP_ID}.sendbird.com/v3/users?limit=100`;
  const res = await fetch(url, {
    headers: { "Api-Token": API_TOKEN },
  });
  const data = await res.json();

  if (!res.ok) {
    console.error("조회 실패:", data);
    return;
  }

  console.log(`총 유저 수: ${data.users.length}`);
  console.log("─".repeat(50));
  for (const u of data.users) {
    console.log(`user_id: "${u.user_id}"  |  nickname: ${u.nickname}`);
  }
}

main().catch(console.error);
