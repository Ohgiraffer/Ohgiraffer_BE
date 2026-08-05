package com.ohgiraffer.notice.domain.repository;

import java.util.Collection;
import java.util.Set;

/**
 * 공지 확인 기록 영속성 포트.
 *
 * <p>확인 방식이 체크박스로 정해져 화면에 필요한 건 "내가 확인했는지"와 "몇 명이 확인했는지"뿐이다.
 * 확인자 명단은 사용자 이름이 필요해 인증 도메인에 조회 유스케이스가 생긴 뒤에 다룬다.
 */
public interface NoticeConfirmationRepository {

    /**
     * 이미 확인한 기록이 있으면 아무 일도 하지 않는다. 여러 번 눌러도 결과가 같다.
     */
    void confirm(Long noticeId, Long userId);

    boolean existsBy(Long noticeId, Long userId);

    long countBy(Long noticeId);

    /**
     * 목록 화면에서 공지마다 질의하지 않도록, 주어진 공지들 중 사용자가 확인한 것만 한 번에 가져온다.
     */
    Set<Long> findConfirmedNoticeIds(Long userId, Collection<Long> noticeIds);
}
