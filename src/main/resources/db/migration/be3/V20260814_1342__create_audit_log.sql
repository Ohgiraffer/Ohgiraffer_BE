CREATE TABLE `audit_log`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '감사로그아이디',
    `domain_name`   VARCHAR(50)  NOT NULL COMMENT '도메인명',
    `event_type`    VARCHAR(50)  NOT NULL COMMENT '이벤트타입',
    `actor_id`      BIGINT       NULL COMMENT '실행자아이디',
    `target_id`     VARCHAR(100) NULL COMMENT '대상리소스아이디',
    `before_value`  TEXT         NULL COMMENT '변경전값',
    `after_value`   TEXT         NULL COMMENT '변경후값',
    `previous_hash` VARCHAR(64)  NOT NULL COMMENT '이전로그해시',
    `hash`          VARCHAR(64)  NOT NULL COMMENT '현재로그해시(HMAC-SHA256)',
    `occurred_at`   DATETIME     NOT NULL COMMENT '발생일시',
    PRIMARY KEY (`id`),
    INDEX `IDX_audit_log_domain_id` (`domain_name`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '해시체인 기반 감사로그';