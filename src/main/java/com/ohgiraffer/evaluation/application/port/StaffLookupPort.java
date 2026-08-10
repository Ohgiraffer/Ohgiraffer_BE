package com.ohgiraffer.evaluation.application.port;

import java.util.List;

/**
 * 알림을 받을 운영진을 찾는다. 사용자 도메인에서 오는 값이라 포트로 둔다.
 *
 * <p>평가 관리 화면은 운영진만 볼 수 있어 알림도 운영진끼리 주고받는다.
 * 훈련생에게는 보내지 않는다.
 */
public interface StaffLookupPort {

    /**
     * 재원 중인 강사와 매니저의 식별자.
     *
     * <p>퇴사하거나 상태가 바뀐 사람은 빼고 가져온다. 받을 수 없는 사람에게 알림을 만들면
     * 읽지 않은 알림만 쌓인다.
     */
    List<Long> findActiveStaffIds();
}
