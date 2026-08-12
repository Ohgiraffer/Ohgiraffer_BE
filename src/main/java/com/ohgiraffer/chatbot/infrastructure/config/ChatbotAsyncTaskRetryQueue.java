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
 *  - execute()가 attempt 증가와 MAX_ATTEMPTS 강제를 담당하는 단일 경로임.
 *    최초 진입(RejectedExecutionHandler -> offer)과 재시도(drain) 양쪽 다 이 메서드를 거치므로
 *    attempt=0으로 재적재해서 한도를 무력화하는 경로가 생기지 않음.
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

    // executor가 작업을 거부하면 여기로 넘어옴 (RejectedExecutionHandler에서 호출됨)
    // 실행 자체를 시도하지 않고 바로 대기열에만 적재 - 최초 시도는 attempt=0
    public void offer(Runnable task) {
        enqueue(new RetryEntry(task, 0));
    }

    // 호출 시작 시점의 대기열 크기만큼만 처리 - execute()에서 재적재된 항목은 batchSize에 포함되지 않아
    // 같은 호출 안에서 즉시 재폴링되지 않고 다음 5초 스케줄로 넘어감 (딜레이 없는 즉시 반복 제출 방지)
    @Scheduled(fixedDelay = 5000)
    public void drain() {
        int batchSize = pending.size();
        for (int i = 0; i < batchSize; i++) {
            RetryEntry entry = pending.poll();
            if (entry == null) {
                break;
            }
            execute(entry);
        }
    }

    // 실행 실패(RejectedExecutionException) 처리를 담당하는 단일 진입점
    // - 기존 entry의 attempt를 그대로 이어받아 1만 증가시킴 (attempt=0으로 되돌리는 경로 없음)
    // - MAX_ATTEMPTS 도달 시 더 이상 큐에 넣지 않고 포기
    private void execute(RetryEntry entry) {
        try {
            chatbotTaskExecutorProvider.getObject().execute(entry.task());
        } catch (RejectedExecutionException e) {
            int nextAttempt = entry.attempt() + 1;
            if (nextAttempt >= MAX_ATTEMPTS) {
                log.error("[ChatbotAsync] 재시도 {}회 모두 실패 - 작업 소실, 수동 확인 필요", MAX_ATTEMPTS);
                return;
            }
            enqueue(new RetryEntry(entry.task(), nextAttempt));
        }
    }

    // 대기열 적재 공통 처리 - 대기열마저 가득 차서 적재 자체가 실패하면 조용히 넘어가지 않고 반드시 에러로 남김
    private void enqueue(RetryEntry entry) {
        boolean added = pending.offer(entry);
        if (!added) {
            log.error("[ChatbotAsync] 재시도 대기열이 가득 차서 작업이 완전히 소실됨 - 즉시 확인 필요 | attempt={}", entry.attempt());
        } else if (entry.attempt() == 0) {
            log.warn("[ChatbotAsync] 실행기 포화로 작업 거부됨 - 재시도 대기열에 적재");
        }
    }

    private record RetryEntry(Runnable task, int attempt) {}

}
