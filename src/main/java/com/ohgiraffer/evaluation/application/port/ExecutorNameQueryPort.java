package com.ohgiraffer.evaluation.application.port;

import java.util.Collection;
import java.util.Map;

/**
 * 동기화를 실행한 사람의 이름을 가져온다. 사용자 도메인에서 오는 값이라 포트로 둔다.
 *
 * <p>이력에는 식별자만 남긴다. 이름을 함께 저장하면 나중에 이름이 바뀌었을 때
 * 어느 쪽이 맞는지 알 수 없다. 보여줄 때 그때의 이름을 가져온다.
 */
public interface ExecutorNameQueryPort {

    /**
     * 식별자마다 이름을 찾아 돌려준다. 찾지 못한 것은 결과에 담기지 않는다.
     */
    Map<Long, String> findNames(Collection<Long> userIds);
}
