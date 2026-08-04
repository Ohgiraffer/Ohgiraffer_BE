-- ============================================================
-- 공지 도메인 기반 컬럼 추가 + 기본 카테고리 시드
-- 담당: be2 (박정민 / 공지·캘린더·평가)
--
-- [사유]
--  1. global/entity/BaseTimeEntity 가 created_at, updated_at 을
--     nullable = false 로 매핑하므로 컬럼이 없으면 엔티티 매핑이 실패한다.
--  2. 요구사항: 공지 목록 "등록일", 상세 "작성일/수정일",
--     메인 요약 "필수 공지(3일 전꺼까지만)" -> created_at 없이는 구현 불가.
--  3. 요구사항: 공지 카테고리 "전체(기본 제공, 삭제 불가)"
--     notice.notice_category_id 가 NOT NULL FK 라 기본 행이 최소 1건 필요하다.
--  4. notice_category.name UNIQUE 는 ERD v5에 있었으나 v6에서 누락됨.
--     중복 이름 허용 시 공지 목록 탭이 중복 노출된다.
-- ============================================================

ALTER TABLE `notice`
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시';

ALTER TABLE `notice_category`
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시';

ALTER TABLE `notice_category`
    ADD CONSTRAINT `UQ_NOTICE_CATEGORY_NAME` UNIQUE (`name`);

-- 기본 카테고리 '전체' (is_default = TRUE -> 수정·삭제 불가 대상)
-- PK를 명시해서 AUTO_INCREMENT 적용 여부와 무관하게 동작하도록 한다.
INSERT IGNORE INTO `notice_category` (`notice_category_id`, `name`, `is_default`)
VALUES (1, '전체', TRUE);
