package com.ohgiraffer.chatbot.application.port;

import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.user.domain.model.Role;

import java.time.LocalDate;

/*
 * comment.
 *  AI비서(챗봇) 도메인이 정의하는 결재 액션 포트
 *  - 전자결재 담당 도메인의 Command UseCase 4종(승인/반려/확인/휴가신청)을 감싸는 계약
 *  - TodoPort들과 동일한 패턴: 챗봇은 이 인터페이스만 알고, 구현은 전자결재 도메인 쪽 Adapter가 채움
 */

public interface ApprovalActionPort {

    // 결재 승인 처리
    CreateApprovalResult approve(Long userId, Role role, Long approvalId);

    // 결재 반려 처리 - 반려 사유 필수
    CreateApprovalResult reject(Long userId, Role role, Long approvalId, String reason);

    // 결재 확인 처리
    CreateApprovalResult check(Long userId, Role role, Long approvalId);

    // 휴가 신청 - 전자서명은 서버가 자동 조회하므로 파라미터로 받지 않음
    CreateApprovalResult createLeaveApproval(Long userId, LocalDate startDate, LocalDate endDate);

}
