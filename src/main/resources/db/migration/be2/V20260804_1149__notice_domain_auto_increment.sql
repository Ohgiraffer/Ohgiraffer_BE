-- ============================================================
-- 공지 / 캘린더 / 평가 도메인 PK AUTO_INCREMENT 추가
-- 담당: be2 (박정민)
--
-- [사유]
--  baseline 스키마의 PK에 AUTO_INCREMENT 가 없어
--  JPA @GeneratedValue(strategy = IDENTITY) 로 저장이 불가능하다.
--  앱 기동과 마이그레이션은 성공하지만 첫 INSERT 에서 아래와 같이 실패한다.
--    ERROR 1364 (HY000): Field 'notice_id' doesn't have a default value
--
-- [범위]
--  be2 담당 테이블만 처리한다. 다른 도메인 테이블은 각 담당자가
--  본인 폴더(be1, be3, be4, be5)에서 동일하게 추가해야 한다.
--
-- [제외]
--  notice_confirmation : 복합 PK (notice_id, user_id) 라 채번 대상이 아님
--
-- [FOREIGN_KEY_CHECKS 해제 이유]
--  MySQL 은 FK 가 참조 중인 컬럼을 MODIFY 할 수 없다.
--    ERROR 1833: Cannot change column 'notice_id':
--                used in a foreign key constraint ... of table 'notice_attachment'
--  컬럼 타입(BIGINT)은 그대로 두고 AUTO_INCREMENT 속성만 추가하므로
--  참조 무결성에는 영향이 없다. 스크립트 끝에서 다시 활성화한다.
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `notice`                     MODIFY COLUMN `notice_id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지사항아이디';
ALTER TABLE `notice_category`            MODIFY COLUMN `notice_category_id`   BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지카테고리아이디';
ALTER TABLE `notice_attachment`          MODIFY COLUMN `notice_attachment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지첨부파일아이디';
ALTER TABLE `notice_ai_event_candidate`  MODIFY COLUMN `candidate_id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '후보아이디';
ALTER TABLE `calendar_event`             MODIFY COLUMN `calendar_event_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '캘린더일정아이디';
ALTER TABLE `evaluation_record`          MODIFY COLUMN `evaluation_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '평가기록아이디';

-- external_sheet_link / sheet_sync_log 는 평가(be2)와 예산(be1)이 함께 사용한다.
-- 동일한 MODIFY 를 be1 에서 또 실행해도 결과가 같아(멱등) 충돌하지 않는다.
ALTER TABLE `external_sheet_link`        MODIFY COLUMN `sheet_link_id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT '시트연동아이디';
ALTER TABLE `sheet_sync_log`             MODIFY COLUMN `sync_log_id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '동기화이력아이디';

SET FOREIGN_KEY_CHECKS = 1;
