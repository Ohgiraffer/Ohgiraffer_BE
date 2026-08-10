package com.ohgiraffer.notification.domain.model;

/*
 * comment.
 *  알림 유형 ENUM
 *  - CONSULTATION_CANCEL(상담예약취소 한정) -> CONSULTATION(상담 전체)으로 팀 확정 변경 반영
 *  - CHAT_MENTION은 CHAT-004(멘션 발생) -> NOTI-006 내부 트리거로 연결됨, 별도 유저노출 API 없음
 * */

public enum NotificationType {

    // 결재 요청 발생
    APPROVAL_REQUEST,

    // 담당자 확인 필요
    NOTICE_CONFIRMATION,

    // 결재 승인/반려 결과
    APPROVAL_RESULT,

    // 출결 주의/경고 단계 근접
    ATTENDANCE_RISK,

    // 채팅 멘션
    CHAT_MENTION,

    // 공지사항 등록
    NOTICE,

    // 캘린더 일정 등록/변경
    CALENDAR_EVENT,

    // 상담 관련 이벤트
    CONSULTATION,

    // 제출 마감 임박
    SUBMISSION_DEADLINE,

    // 평가 시트 동기화로 평가 내용이 바뀜 (be2 박정민 추가)
    EVALUATION

}
