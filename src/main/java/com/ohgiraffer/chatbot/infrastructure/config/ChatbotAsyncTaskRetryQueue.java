package com.ohgiraffer.chatbot.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/*
 * comment.
 *  chatbotTaskExecutor가 포화(core+max 스레드 및 queue 50 모두 사용 중) 상태일 때
 *  기본 AbortPolicy가 작업을 즉시 버리는 것을 막기 위한 재시도 대기열
 *  - CallerRunsPolicy는 쓰지 않음 (요청 스레드가 대신 실행하게 되면 애초에 @Async로 분리한 의도가 무색해짐)
 *  - 인메모리 큐라서 앱이 재시작되면 대기 중이던 작업은 그대로 유실됨.
 *    완전한 영구 보존(진짜 durable)은 DB 기반 outbox가 필요하고, 이는 별도 스코프로 보류된 상태 -
 *    이 클래스는 그 사이의 임시 안전판(순간적인 포화만 커버) 역할임
 *  - ObjectProvider로 실제 사용 시점까지 조회를 늦춰서 순환을 끊음 (@Lazy 매개변수 방식은 IDE/컴파일 에러 발생해서 미사용)
 */

@Slf4j
@Component
public class ChatbotAsyncTaskRetryQueue {

    private static final int MAX_ATTEMPTS = 3;
    private static final int QUEUE_CAPACITY = 100;

    private final BlockingQueue<RetryEntry> pending = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ObjectProvider<TaskExecutor> chatbotTaskExecutorProvider;

    public ChatbotAsyncTaskRetryQueue(
            @Qualifier("chatbotTaskExecutor") ObjectProvider<TaskExecutor> chatbotTaskExecutorProvider) {
        this.chatbotTaskExecutorProvider = chatbotTaskExecutorProvider;
    }

    // executor가 작업을 거부하면 즉시 버리지 않고 여기로 적재 (RejectedExecutionHandler에서 호출됨)
    public void offer(Runnable task) {
        boolean added = pending.offer(new RetryEntry(task, 0));
        if (!added) {
            // 재시도 대기열 자체도 가득 찬 극단적 상황 - 더 이상 손쓸 방법 없이 소실됨, 즉시 확인 필요
            log.error("[ChatbotAsync] 재시도 대기열도 가득 차서 작업이 완전히 소실됨 - 즉시 확인 필요");
        } else {
            log.warn("[ChatbotAsync] 실행기 포화로 작업 거부됨 - 재시도 대기열에 적재");
        }
    }

    // 주기적으로 대기열을 비우며 재제출 시도. 여전히 포화 상태면 시도 횟수를 늘려 다시 대기, 한도 초과 시 포기
    @Scheduled(fixedDelay = 5000)
    public void drain() {
        RetryEntry entry;
        while ((entry = pending.poll()) != null) {
            try {
                chatbotTaskExecutorProvider.getObject().execute(entry.task());
            } catch (RejectedExecutionException e) {
                int nextAttempt = entry.attempt() + 1;
                if (nextAttempt < MAX_ATTEMPTS) {
                    boolean reAdded = pending.offer(new RetryEntry(entry.task(), nextAttempt));
                    if (!reAdded) {
                        // 재대기 시점에 큐가 이미 가득 찬 경우 - 조용히 넘어가면 offer()와 동일한 소실이
                        // 여기서도 재발하므로 반드시 로그로 남김
                        log.error("[ChatbotAsync] 재시도 대기열이 가득 차서 재적재 실패 - 작업 소실, 즉시 확인 필요");
                    }
                } else {
                    log.error("[ChatbotAsync] 재시도 {}회 모두 실패 - 작업 소실, 수동 확인 필요", MAX_ATTEMPTS);
                }
            }
        }
    }

    private record RetryEntry(Runnable task, int attempt) {}

}
