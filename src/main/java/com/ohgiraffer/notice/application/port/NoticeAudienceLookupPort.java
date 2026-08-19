package com.ohgiraffer.notice.application.port;

import java.util.List;

/*
 * comment.
 *  공지 등록 시 알림 받을 전체 대상(훈련생+강사+매니저)을 조회하는 포트.
 *  공지는 부트캠프 전체 공개이므로 역할 구분 없이 같은 부트캠프 유저 전원을 대상으로 한다.
 */

public interface NoticeAudienceLookupPort {

    // authorId와 같은 부트캠프 유저 중, 공지가 비공개(훈련생 제외)면 훈련생 빼고 조회
    List<Long> findAllUserIdsInSameBootcamp(Long authorId, boolean visibleToTrainee);

}
