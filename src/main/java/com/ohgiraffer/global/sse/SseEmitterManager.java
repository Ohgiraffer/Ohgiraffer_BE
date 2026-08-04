package com.ohgiraffer.global.sse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/*
* comment.
*  SSE 커넥션을 관리하는 매니저
*  - userId 기준으로 emitter를 저장/조회/삭제함
*  - 유저당 다중 연결(멀티탭, 멀티기기) 지원
*  - 30초 주기 하트비트로 프록시 idle timeout 방지 + 죽은 커넥션 조기 감지
*  - 나중에 서버 여러 대로 스케일아웃할 땐 이 클래스를 Redis Pub/Sub 기반 구현체로 교체
* */

@Component
public class SseEmitterManager implements SseEventPublisher{

    // SSE 커넥션 자체의 타임아웃 (1시간). 이 시간 지나면 브라우저가 자동 재연결 시도함
    private static final Long TIMEOUT = 60L * 1000 * 60;
    // 하트비트 전송 주기 (초). 프록시/ALB idle timeout(보통 60초)의 절반으로 설정
    private static final long HEARTBEAT_INTERVAL = 30L;

    // userId -> 해당 유저의 emitter 목록 (탭/기기별로 여러 개 가능)
    // ConcurrentHashMap: 여러 스레드가 동시에 연결/해제해도 안전
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    // 하트비트 전용 단일 스레드 스케줄러
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 빈 생성 직후 하트비트 스케줄러 시작
     * 30초마다 전체 emitter한테 더미 이벤트를 쏴서 연결 살아있음을 알림
     */
    @PostConstruct
    public void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(
                this::sendHeartbeatToAll,
                HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL,
                TimeUnit.SECONDS
        );
    }

    /**
     * 애플리케이션 종료 시 스케줄러도 같이 종료
     * 안 해주면 스레드가 안 죽고 계속 남아있음
     */
    @PreDestroy
    public void stopHeartbeat() {
        heartbeatExecutor.shutdown();
    }

    /**
     * 전체 유저의 전체 emitter를 순회하며 하트비트 전송
     * 전송 중 끊긴 게 확인되면 sendEvent 내부에서 바로 정리됨
     */
    private void sendHeartbeatToAll() {
        emitters.forEach((userId, userEmitters) ->
                userEmitters.forEach(emitter -> sendEvent(userId, emitter, "heartbeat", "ping"))
        );
    }

    /**
     * 클라이언트의 SSE 구독 요청 처리
     * 새 emitter 생성 → 해당 유저의 리스트에 추가 → 완료/타임아웃/에러 콜백에 정리 로직 등록
     *
     * @param userId 구독하는 유저 ID (인증 붙으면 여기로 실제 값 들어옴)
     * @return 컨트롤러가 그대로 응답으로 반환할 emitter
     */
    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // compute로 리스트 조회+추가를 원자적으로 처리
        // removeEmitter와 동시에 실행돼도새로 추가되는 emitter가 유실되지 않도록 함
        // CopyOnWriteArrayList: 읽기(순회)가 훨씬 잦고 쓰기(추가/삭제)가 적은 상황에 적합
        emitters.compute(userId, (id, list) -> {
            List<SseEmitter> target = (list != null) ? list : new CopyOnWriteArrayList<>();
            target.add(emitter);
            return target;
        });

        // 연결 정상 종료/타임아웃/에러 시 리스트에서 해당 emitter 제거
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        // 연결 직후 더미 이벤트 전송 — 일부 프록시가 빈 응답을 끊어버리는 것 방지
        sendEvent(userId, emitter, "connect", "connected");
        return emitter;
    }

    /**
     * 타 도메인(알림 등)이 특정 유저에게 실시간 이벤트를 발행할 때 호출하는 진입점
     * 이 메서드 하나만 의존하면 되고, SSE 내부 구현은 몰라도 됨
     */
    @Override
    public void publish(Long userId, String eventName, Object data) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) return; // 접속 중인 커넥션 없으면 그냥 무시

        userEmitters.forEach(emitter -> sendEvent(userId, emitter, eventName, data));
    }

    /**
     * 실제 이벤트 전송 로직
     * 전송 실패 시(끊긴 연결 IOException, 이미 종료된 emitter에 대한 IllegalStateException)
     * 모두 리스트에서 제거만 하고 completeWithError는 호출하지 않음
     * 하트비트 스케줄러 도중 예외가 재발생하면 scheduleAtFixedRate 자체가 멈춰버리기 때문
     */
    private void sendEvent(Long userId, SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException | IllegalStateException e) {
            removeEmitter(userId, emitter);
        }
    }

    /**
     * 특정 유저의 특정 emitter를 리스트에서 제거
     * computeIfPresent로 조회+삭제를 원자적으로 처리
     * connect의 compute와 동시에 실행돼도 서로 경합하지 않음
     * 리스트가 비면(마지막 연결까지 끊기면) 맵 엔트리 자체도 삭제
     */
    private void removeEmitter(Long userId, SseEmitter emitter) {
        emitters.computeIfPresent(userId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

}
