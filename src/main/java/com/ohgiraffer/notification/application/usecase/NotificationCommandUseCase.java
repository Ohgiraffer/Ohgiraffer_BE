package com.ohgiraffer.notification.application.usecase;

import com.ohgiraffer.notification.application.command.NotificationBulkDeleteCommand;
import com.ohgiraffer.notification.application.command.NotificationCreateCommand;
import com.ohgiraffer.notification.application.result.NotificationResult;

public interface NotificationCommandUseCase {

    // 알림 생성 - 내부 트리거 전용, notificationOn 값과 무관하게 항상 저장됨
    NotificationResult create(NotificationCreateCommand command);

    // 읽음 처리 - requesterId로 본인 소유 검증 후 처리, 예외 가능
    NotificationResult markAsRead(Long notificationId, Long requesterId);

    // 개별 삭제 - 하드 딜리트, requesterId로 본인 소유 검증
    void delete(Long notificationId, Long requesterId);

    // 선택 삭제 - 하드 딜리트, 실제 삭제된 건수 반환
    long bulkDelete(NotificationBulkDeleteCommand command);

}
