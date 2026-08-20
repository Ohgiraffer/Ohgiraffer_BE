package com.ohgiraffer.evaluation.application.port;

import com.ohgiraffer.evaluation.domain.model.EvaluationChange;

import java.util.List;
import java.util.Map;

/**
 * 변경 내역에서 운영진이 확인해야 할 점을 짚는다. 외부 AI 를 쓰므로 포트로 둔다.
 *
 * <p>무엇이 어떻게 바뀌었는지는 이미 값으로 들고 있어 AI 에게 물을 것이 없다. 여기서만
 * AI 를 쓰는 이유는 "0점인데 사유가 없다" 처럼 사람이 들여다봐야 할 대목을 골라내는 일이
 * 규칙으로 적기 어렵기 때문이다.
 *
 * <p>포트로 나눈 이유는 두 가지다. 하나는 동기화 로직을 시험할 때 실제 AI 를 부르지 않기
 * 위해서고, 다른 하나는 실패했을 때 부르는 쪽이 그냥 넘어갈 수 있게 하기 위해서다.
 */
public interface EvaluationSummaryPort {

    /**
     * 확인이 필요한 훈련생만 골라 돌려준다. 실패하면 예외를 던진다.
     *
     * <p>부르는 쪽이 실패를 받아 확인 필요 없이 진행한다. 여기서 조용히 빈 값을 돌려주면
     * AI 가 짚을 것이 없었던 것인지 호출이 실패한 것인지 구분할 수 없다.
     *
     * @return 훈련생 이름 → 확인해야 할 점. 짚을 것이 없는 훈련생은 담기지 않는다
     */
    Map<String, String> findPointsToCheck(List<EvaluationChange> changes);
}
