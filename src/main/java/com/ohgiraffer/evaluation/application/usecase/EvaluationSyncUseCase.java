package com.ohgiraffer.evaluation.application.usecase;

import com.ohgiraffer.evaluation.application.query.EvaluationSyncResult;

public interface EvaluationSyncUseCase {

    /**
     * 저장된 연동 설정으로 시트를 읽어 평가 데이터를 최신화한다.
     *
     * <p>같은 평가를 두 번 저장하지 않도록 시트 행 식별값으로 대조한다.
     * 반영하지 못한 행은 건너뛰고 결과에 담는다.
     *
     * @param executedBy 실행한 사람. 이력에 남는다
     */
    EvaluationSyncResult sync(Long executedBy);
}
