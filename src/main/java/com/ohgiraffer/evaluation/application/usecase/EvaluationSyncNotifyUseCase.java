package com.ohgiraffer.evaluation.application.usecase;

public interface EvaluationSyncNotifyUseCase {

    /**
     * 동기화 결과를 운영진에게 알린다.
     *
     * <p>동기화할 때 자동으로 보내지 않는 것은 요구사항이 "버튼을 눌러" 라고 못박아서다.
     * 사람이 요약을 읽고 알릴 만한 내용인지 판단한 뒤 보낸다.
     *
     * @param requesterId 보낸 사람. 자기 자신에게는 보내지 않는다
     * @return 알림을 보낸 사람 수
     */
    int notify(Long syncLogId, Long requesterId);
}
