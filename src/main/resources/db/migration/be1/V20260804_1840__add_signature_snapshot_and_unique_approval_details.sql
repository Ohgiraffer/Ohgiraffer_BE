-- ============================================================
-- 결재 요청 전자서명 스냅샷 및 상세 테이블 유니크 제약 추가
-- - 결재 요청 생성 시점의 전자서명 이미지와 MIME type을 보존한다.
-- - 이후 사용자가 전자서명을 삭제/재등록해도 기존 결재 문서의 서명 증적이 유지된다.
-- - 결재 요청 1건당 휴가 상세/구매 상세는 각각 1건만 허용한다.
-- ============================================================

ALTER TABLE `approval_request`
    ADD COLUMN `signature_image_snapshot` LONGBLOB NULL COMMENT '신청시점전자서명이미지' AFTER `signature_id`,
    ADD COLUMN `signature_file_type_snapshot` VARCHAR(50) NULL COMMENT '신청시점전자서명파일유형' AFTER `signature_image_snapshot`;

ALTER TABLE `approval_leave_detail`
    ADD CONSTRAINT `UQ_APPROVAL_LEAVE_DETAIL_APPROVAL_ID` UNIQUE (`approval_id`);

ALTER TABLE `approval_purchase_detail`
    ADD CONSTRAINT `UQ_APPROVAL_PURCHASE_DETAIL_APPROVAL_ID` UNIQUE (`approval_id`);