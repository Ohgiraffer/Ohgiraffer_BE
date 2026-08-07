-- ============================================================
-- 캘린더 일정: 종일 여부 컬럼과 등록/수정 시각 추가
-- 담당: be2 (박정민)
--
-- [사유]
--  1. 등록 화면의 시작 시각·종료 시각이 선택 입력이다.
--     시간을 넣지 않으면 00:00 으로 저장할 수밖에 없는데, 그러면
--     "종일 일정"과 "자정에 시작하는 일정"을 구분할 수 없다.
--     화면은 둘을 다르게 그려야 하므로 판별용 컬럼을 둔다.
--
--  2. baseline 에 created_at / updated_at 이 없어 BaseTimeEntity 를 쓸 수 없다.
--     공지 계열과 같은 기준으로 맞춘다.
--
--  3. start_time / end_time 은 DATETIME(0) 이라 초 미만이 잘린다.
--     애플리케이션은 Instant 로 시각을 다루므로 저장·조회 값이 어긋난다.
--     이미 DATETIME(6) 으로 통일한 notice 계열과 기준을 맞춘다.
-- ============================================================

ALTER TABLE `calendar_event`
    ADD COLUMN `is_all_day` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '종일여부'
        AFTER `end_time`;

ALTER TABLE `calendar_event`
    ADD COLUMN `created_at` DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    ADD COLUMN `updated_at` DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시';

ALTER TABLE `calendar_event`
    MODIFY COLUMN `start_time` DATETIME(6) NOT NULL COMMENT '시작일시',
    MODIFY COLUMN `end_time`   DATETIME(6) NOT NULL COMMENT '종료일시';
