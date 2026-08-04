-- ============================================================
-- 공지 도메인 시각 컬럼을 마이크로초 정밀도로 변경
-- 담당: be2 (박정민)
--
-- [사유]
--  global/entity/BaseTimeEntity 는 Instant(나노초)로 시각을 다루는데
--  DATETIME 컬럼은 초 단위라 저장 시 반올림된다. 그 결과
--    등록 응답(메모리 값) : 2026-08-04T16:22:40.182
--    상세 조회(DB 값)     : 2026-08-04T16:22:40
--  처럼 같은 공지의 시각이 응답마다 다르게 보이고 최대 1초까지 어긋난다.
--
--  DATETIME(6) 으로 바꾸면 저장 값과 메모리 값이 일치해 문제가 사라진다.
--
-- [범위]
--  be2 담당 테이블 중 BaseTimeEntity 를 상속하는 것만 대상으로 한다.
--  다른 도메인 테이블은 각 담당자가 필요 시 본인 폴더에서 변경한다.
-- ============================================================

ALTER TABLE `notice`
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    MODIFY COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시';

ALTER TABLE `notice_category`
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    MODIFY COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시';
