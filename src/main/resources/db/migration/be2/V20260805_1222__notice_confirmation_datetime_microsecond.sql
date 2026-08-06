-- 공지 확인 일시의 정밀도를 notice 테이블과 맞춘다.
--
-- baseline 의 confirmed_at 은 DATETIME(0) 이라 초 미만이 잘린다.
-- 애플리케이션은 Instant(나노초)로 시각을 다루므로 저장·조회 값이 어긋나고,
-- 같은 초에 여러 건이 들어오면 확인 순서를 구분할 수 없다.
-- 이미 DATETIME(6) 으로 통일한 notice / notice_category 와 기준을 맞춘다.
--
-- DEFAULT CURRENT_TIMESTAMP 도 같은 정밀도로 다시 지정해야 한다.
-- 정밀도만 바꾸고 기본값을 두면 CURRENT_TIMESTAMP(0) 이 남아 서로 어긋난다.

ALTER TABLE `notice_confirmation`
    MODIFY COLUMN `confirmed_at` DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) COMMENT '확인일시';
