package com.ohgiraffer.notice.application.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션이 커밋된 뒤에 실행할 일을 등록한다.
 *
 * <p>저장소 파일 삭제처럼 트랜잭션에 참여하지 않는 작업에 쓴다. 커밋 전에 지우면 그 뒤
 * 트랜잭션이 되돌아갔을 때 DB 행은 살아나지만 파일은 사라져, 목록에는 보이는데
 * 내려받을 수 없는 첨부가 남는다.
 *
 * <p>트랜잭션 밖에서 부르면 그 자리에서 바로 실행한다. 커밋을 기다릴 것이 없기 때문이다.
 */
final class AfterCommit {

    private AfterCommit() {
    }

    static void run(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
}
