-- ============================================================
-- 시트 동기화 이력에 실행자와 변경 건수 추가
-- 담당: be2 (박정민)
--
-- [사유]
--  평가관리 이력 목록 화면이 "날짜 / 실행자 / 건수 / 상세보기" 를 보여준다.
--  baseline 의 sheet_sync_log 에는 날짜(synced_at)와 요약(diff_summary)만 있어
--  실행자와 건수를 낼 수가 없다.
--
--  diff_summary 에 함께 넣는 방법도 있으나, 그 칸은 AI 가 만든 요약문이 들어가는 자리다.
--  메타데이터를 섞으면 목록을 그릴 때마다 TEXT 를 파싱해야 하고 정렬·집계도 안 된다.
--
-- [executed_by 가 NULL 을 허용하는 이유]
--  사람이 버튼을 눌러 실행하면 그 사람이 담기지만, 나중에 주기 실행을 붙이면
--  실행한 사람이 없다. 그때 NULL 로 두고 화면이 "시스템" 으로 표시하면 된다.
--
--  users 를 참조하되 ON DELETE SET NULL 이다. 실행자가 탈퇴해도 이력 자체는
--  남아야 한다. 언제 무엇이 바뀌었는지가 이력의 본체이고 누가 눌렀는지는 부가 정보다.
--
-- [changed_count]
--  추가와 수정을 합친 수다. 화면의 "4건" 이 이 값이다.
--  집계를 매번 다시 하지 않으려고 저장해 둔다. 이력은 그 시점의 기록이라
--  나중에 세면 값이 달라진다.
-- ============================================================

ALTER TABLE `sheet_sync_log`
    ADD COLUMN `executed_by` BIGINT NULL COMMENT '실행자아이디' AFTER `sheet_link_id`,
    ADD COLUMN `changed_count` INT NOT NULL DEFAULT 0 COMMENT '변경건수' AFTER `executed_by`;

ALTER TABLE `sheet_sync_log`
    ADD CONSTRAINT `FK_users_TO_sheet_sync_log_1`
        FOREIGN KEY (`executed_by`) REFERENCES `users` (`user_id`)
        ON DELETE SET NULL;
