package com.ohgiraffer.attendance.application.port;

import com.ohgiraffer.notification.domain.model.NotificationType;

public interface NotifiedTodayCheckPort {

    // 오늘 날짜 기준, 해당 유저에게 해당 타입 알림이 이미 발송됐는지 확인 (중복 발송 방지용)
    boolean isNotifiedToday(Long userId, NotificationType notificationType);

}
