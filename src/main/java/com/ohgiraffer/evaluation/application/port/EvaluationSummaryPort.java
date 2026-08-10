package com.ohgiraffer.evaluation.application.port;

import com.ohgiraffer.evaluation.domain.model.EvaluationChange;

import java.util.List;

/**
 * 변경 내역을 사람이 읽기 좋은 요약으로 바꾼다. 외부 AI 를 쓰므로 포트로 둔다.
 *
 * <p>포트로 나눈 이유는 두 가지다. 하나는 동기화 로직을 시험할 때 실제 AI 를 부르지 않기
 * 위해서고, 다른 하나는 요약이 실패했을 때 다른 방법으로 되돌릴 수 있게 하기 위해서다.
 */
public interface EvaluationSummaryPort {

    /**
     * 요약문을 만든다. 실패하면 예외를 던진다.
     *
     * <p>부르는 쪽이 실패를 받아 대비책으로 넘어간다. 여기서 조용히 빈 문자열을 돌려주면
     * 요약이 없는 이력이 남고, 왜 비었는지 알 수 없다.
     */
    String summarize(List<EvaluationChange> changes);
}
