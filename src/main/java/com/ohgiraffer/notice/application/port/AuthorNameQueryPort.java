package com.ohgiraffer.notice.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 공지 작성자의 이름을 사용자 도메인에서 가져오는 포트.
 *
 * <p>공지는 작성자를 식별자로만 들고 있는데 화면에는 이름이 나가야 한다.
 * 사용자 도메인을 직접 부르지 않고 이 포트를 거치는 이유는, 공지 쪽 계층이
 * 사용자 도메인의 모델·저장소 구조에 묶이지 않게 하기 위해서다.
 * 팀 규칙("정보 필요한 쪽이 알아서 작업하고 연락")에 따라 공지 쪽에서 정의했다.
 */
public interface AuthorNameQueryPort {

    Optional<String> findName(Long authorId);

    /**
     * 목록처럼 작성자가 여럿일 때 쓴다. 찾지 못한 식별자는 결과에 담기지 않는다.
     */
    Map<Long, String> findNames(Collection<Long> authorIds);
}
