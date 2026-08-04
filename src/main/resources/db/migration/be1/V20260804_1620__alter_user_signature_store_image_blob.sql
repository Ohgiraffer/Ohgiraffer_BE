-- ============================================================
-- user_signature 테이블 전자서명 이미지 저장 방식 변경
-- - 기존 URL 저장 방식 제거
-- - DB BLOB 저장 방식으로 변경
-- - signature_id AUTO_INCREMENT 적용
-- - 사용자당 전자서명 1개 관리
-- ============================================================

ALTER TABLE `user_signature`
DROP COLUMN `signature_image_url`;

ALTER TABLE `user_signature`
    ADD COLUMN `signature_image` LONGBLOB NOT NULL COMMENT '전자서명이미지' AFTER `signature_id`;

ALTER TABLE `user_signature`
    MODIFY COLUMN `signature_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '전자서명아이디',
    MODIFY COLUMN `original_file_name` VARCHAR(255) NOT NULL COMMENT '원본파일명',
    MODIFY COLUMN `file_size_bytes` BIGINT NOT NULL COMMENT '파일크기',
    MODIFY COLUMN `file_type` VARCHAR(50) NOT NULL COMMENT '파일유형',
    MODIFY COLUMN `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성여부',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시';

ALTER TABLE `user_signature`
    ADD CONSTRAINT `UQ_USER_SIGNATURE_USER_ID` UNIQUE (`user_id`);