package com.ohgiraffer.todo.domain.model;

/* comment.
 *  TodoItemResponse의 sourceDomain 필드용 enum
 *  - 어느 팀원 도메인에서 파생된 TODO 항목인지 식별
 *  - 6개 Port(Submission/Approval/Notice/Evaluation/Consultation/Attendance)와 1:1 매핑
 */

public enum TodoSourceDomain {
    SUBMISSION,     // 제출물 관리 (발표자료/평가만족도)
    APPROVAL,       // 전자결재 (휴가/예산 신청)
    NOTICE,         // 공지사항
    CONSULTATION,   // 상담 관리
}
