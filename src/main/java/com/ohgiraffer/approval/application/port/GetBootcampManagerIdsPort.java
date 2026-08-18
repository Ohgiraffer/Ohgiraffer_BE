package com.ohgiraffer.approval.application.port;

import java.util.List;

/*
 * comment.
 *  결재 신청(휴가/구매) 시 부트캠프의 매니저 전체에게 알림을 보내기 위한 조회 포트.
 *  결재자(approverId)는 check() 시점에야 확정되므로, 신청 시점엔 매니저 전원에게 브로드캐스트함.
 */

public interface GetBootcampManagerIdsPort {

    // 해당 부트캠프에 속한 매니저(MANAGER role) 유저 아이디 전체 조회
    List<Long> findManagerIdsByBootcampId(Long bootcampId);

}
