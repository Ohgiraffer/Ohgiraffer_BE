package com.ohgiraffer.notification.application.command;

import java.util.List;

/*
 * comment.
 *  알림 선택 삭제 커맨드
 *  requesterId는 인증 컨텍스트에서 주입 - 본인 소유 아닌 id가 섞여도 조용히 스킵되고 실제 삭제된 것만 개수로 반환됨
 */

public record NotificationBulkDeleteCommand(
        List<Long> notificationIds,
        Long requesterId
) {
}
