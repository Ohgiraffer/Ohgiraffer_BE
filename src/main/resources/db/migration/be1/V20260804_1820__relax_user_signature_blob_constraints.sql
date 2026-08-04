-- ============================================================
-- user_signature 기존 데이터 보호를 위한 보정 migration
-- - 이전 migration에서 삭제된 signature_image_url 컬럼을 복구한다.
-- - 기존 URL 기반 전자서명 데이터가 있을 수 있으므로
--   signature_image 및 파일 메타데이터 컬럼의 NOT NULL 제약을 완화한다.
-- - signature_image_url 삭제와 signature_image NOT NULL 강제는
--   데이터 검증 이후 별도 migration에서 처리한다.
-- ============================================================

ALTER TABLE `user_signature`
    ADD COLUMN `signature_image_url` VARCHAR(500) NULL COMMENT '전자서명이미지 URL' AFTER `signature_id`;

ALTER TABLE `user_signature`
    MODIFY COLUMN `signature_image` LONGBLOB NULL COMMENT '전자서명이미지',
    MODIFY COLUMN `original_file_name` VARCHAR(255) NULL COMMENT '원본파일명',
    MODIFY COLUMN `file_size_bytes` BIGINT NULL COMMENT '파일크기',
    MODIFY COLUMN `file_type` VARCHAR(50) NULL COMMENT '파일유형',
    MODIFY COLUMN `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성여부',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시';