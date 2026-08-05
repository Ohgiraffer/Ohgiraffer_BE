package com.ohgiraffer.notice.domain.model;

/**
 * 공지를 조회하는 사람의 구분.
 *
 * <p>공지 도메인이 필요로 하는 건 "훈련생이냐 아니냐"뿐이라 인증 도메인의 역할 값을
 * 그대로 끌어오지 않고 자체 개념으로 좁혀서 쓴다. 역할이 늘어나도 공지 쪽은 영향받지 않는다.
 */
public enum ViewerRole {

    /**
     * 훈련생. 훈련생 비공개 공지를 볼 수 없다.
     */
    TRAINEE,

    /**
     * 강사·매니저 등 운영진. 모든 공지를 볼 수 있다.
     */
    STAFF
}
