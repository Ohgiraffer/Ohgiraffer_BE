package com.ohgiraffer.notice.application.query;

/**
 * 확인 처리 직후의 확인 현황.
 *
 * <p>화면은 체크박스 옆 인원수를 바로 갱신해야 한다. 확인 요청이 인원수를 함께 돌려주지 않으면
 * 화면에서 1을 더하는 수밖에 없는데, 그 사이 다른 사람이 확인했다면 숫자가 어긋난다.
 * 그래서 확인 처리 결과로 갱신된 현황을 그대로 돌려준다.
 */
public record NoticeConfirmationView(
        Long noticeId,
        long confirmationCount,
        boolean confirmedByMe
) {
}
