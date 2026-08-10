package com.ohgiraffer.evaluation.application.port;

import java.util.Collection;
import java.util.Map;

/**
 * 시트에 적힌 이메일로 훈련생을 찾는다. 사용자 도메인에서 가져오는 값이라 포트로 둔다.
 *
 * <p>이름이 아니라 이메일로 찾는 것은 {@code users.email} 에만 유니크 제약이 있기 때문이다.
 * 이름으로 찾으면 동명이인일 때 조용히 엉뚱한 사람에게 평가가 붙고, 나중에 발견해도
 * 어느 것이 잘못됐는지 알 수 없다.
 */
public interface TraineeLookupPort {

    /**
     * 이메일마다 사용자 식별자를 찾아 돌려준다.
     *
     * <p>한 건씩 묻지 않고 목록으로 받는 것은 시트 한 장에 훈련생이 여럿이기 때문이다.
     * 찾지 못한 이메일은 결과에 담기지 않는다. 그 행을 어떻게 처리할지는 부르는 쪽이 정한다.
     */
    Map<String, Long> findTraineeIdsByEmails(Collection<String> emails);
}
