/**
 * scripts/setup-manager-channel.js
 *
 * 최초 1회만 실행하는 셋업 스크립트.
 * 1. aiops-bot 유저 생성 (이미 있으면 스킵)
 * 2. 매니저들만 있는 그룹 채널 생성
 * 3. 나온 channel_url을 .env의 MANAGER_CHANNEL_URL에 직접 복사해서 넣을 것
 *
 * 실행: node scripts/setup-manager-channel.js <매니저userId1> <매니저userId2> ...
 * (매니저의 sendbirdUserId는 백엔드 유저 테이블 기준 - 프론트 로그인 시 쓰는 값과 동일해야 함)
 *
 * 주의: 이 스크립트를 두 번 실행하면 매니저 채널이 중복 생성된다.
 * MANAGER_CHANNEL_URL이 이미 .env에 있다면 실행하지 말 것.
 */

require("dotenv").config();
const { ensureBotUser, createManagerChannel } = require("../lib/sendbird");

async function main() {
  const managerUserIds = process.argv.slice(2);

  if (managerUserIds.length === 0) {
    console.error("사용법: node scripts/setup-manager-channel.js <매니저userId1> <매니저userId2> ...");
    process.exit(1);
  }

  if (!process.env.MANAGER_CHANNEL_URL || process.env.MANAGER_CHANNEL_URL.trim() === "") {
    // 계속 진행
  } else {
    console.error(
      `[setup] .env에 MANAGER_CHANNEL_URL이 이미 설정되어 있습니다: ${process.env.MANAGER_CHANNEL_URL}\n` +
        `중복 생성을 막기 위해 중단합니다. 채널을 새로 만들려면 .env에서 해당 값을 지우고 다시 실행하세요.`
    );
    process.exit(1);
  }

  console.log("[setup] 봇 유저 확인/생성 중...");
  const botUserId = await ensureBotUser();
  console.log(`[setup] 봇 유저 준비 완료: ${botUserId}`);

  console.log(`[setup] 매니저 채널 생성 중... (멤버: ${managerUserIds.join(", ")})`);
  const channelUrl = await createManagerChannel(managerUserIds);

  console.log("\n=================================================");
  console.log("채널 생성 완료. 아래 값을 .env에 추가하세요:");
  console.log(`MANAGER_CHANNEL_URL=${channelUrl}`);
  console.log("=================================================\n");
}

main().catch((err) => {
  console.error("[setup] 실패:", err);
  process.exit(1);
});
