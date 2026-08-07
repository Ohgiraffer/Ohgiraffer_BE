package com.ohgiraffer.notification.application.port;

/*
 * comment.
 *  user 도메인의 알림 on/off 설정을 조회하는 포트
 *  실제 구현(같은 DB 직접 조회 vs 서비스 간 호출)은 infrastructure에서 확정 후 구현
 */

public interface UserNotificationSettingPort {

    // 해당 유저가 실시간 알림 수신을 켜뒀는지 여부
    boolean isNotificationOn(Long userId);

}
