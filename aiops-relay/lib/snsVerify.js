/**
 * snsVerify.js
 *
 * AWS SNS가 보내는 메시지의 서명을 검증한다.
 * 검증 없이 그냥 받으면, 외부에서 이 웹훅 URL만 알아도 CloudWatch Alarm을 흉내 낸
 * 가짜 알림을 보내 AUTO_EXECUTE 화이트리스트 액션을 실행시킬 수 있음 - 반드시 필요.
 */

const MessageValidator = require("sns-validator");
const validator = new MessageValidator();

function validateSnsMessage(message) {
    return new Promise((resolve, reject) => {
        validator.validate(message, (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

module.exports = { validateSnsMessage };