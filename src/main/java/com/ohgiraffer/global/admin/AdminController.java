package com.ohgiraffer.global.admin;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AIOps 중계서버(aiops-relay)가 호출하는 관리용 엔드포인트 모음.
 *
 * 일반 사용자 인증(JWT)과는 별개로, 서버 대 서버(relay -> backend) 호출만
 * 허용하기 위해 내부 토큰(X-Internal-Token 헤더)으로 보호한다.
 * 이 토큰은 relay 서버의 .env(ADMIN_INTERNAL_TOKEN)와 이 서버의
 * application.yaml(admin.internal-token)이 같은 값을 공유해야 동작한다.
 *
 * 여기 등록되는 조치는 전부 "되돌리기 쉽고 부작용이 국소적인 것"만 다뤄야 한다.
 * (docs/action-risk-policy.md 의 저위험 기준과 일치시킬 것)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final HikariDataSource hikariDataSource;

    @Value("${admin.internal-token}")
    private String internalToken;

    /**
     * HikariCP 커넥션 풀의 유휴 커넥션들을 안전하게 회수(evict)한다.
     * 강제로 끊는 게 아니라, 다음에 반납될 때 새 커넥션으로 교체되도록
     * 유도하는 방식이라 서비스 중단 없이 안전하게 실행 가능하다.
     */
    @PostMapping("/admin/reset-connection-pool")
    public ResponseEntity<Map<String, Object>> resetConnectionPool(
            @RequestHeader(value = "X-Internal-Token", required = false) String token
    ) {
        if (token == null || !token.equals(internalToken)) {
            log.warn("[AdminController] 잘못된 내부 토큰으로 /admin/reset-connection-pool 호출 시도됨");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "인증되지 않은 요청입니다."));
        }

        try {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            int beforeIdle = poolMXBean.getIdleConnections();

            poolMXBean.softEvictConnections();

            log.info("[AdminController] DB 커넥션 풀 리셋 실행됨 (evict 전 idle 커넥션: {})", beforeIdle);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "커넥션 풀 리셋 명령을 전송했습니다.",
                    "idleConnectionsBeforeReset", beforeIdle
            ));
        } catch (Exception e) {
            log.error("[AdminController] 커넥션 풀 리셋 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "리셋 중 오류: " + e.getMessage()));
        }
    }
}
