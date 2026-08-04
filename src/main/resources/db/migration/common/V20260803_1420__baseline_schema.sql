-- ============================================================
-- CampFlow DB Migration Script
-- 작성일: 2026-08-03
-- 원본 ERD Cloud MySQL Export 기반, 팀 공유 + 마이그레이션 적용용 정리본
--
-- [ENUM 값 확인 필요]
-- - users.role: STUDENT/INSTRUCTOR/MANAGER
-- - users.status: ACTIVE(재원 중)/COMPLETED(수료)/WITHDRAWN(자퇴)/EXPELLED(제적)
-- - approval_request.request_type: LEAVE(휴가)/ABSENCE(결석)/EARLY_LEAVE(조퇴)/OUTING(외출)/EXPENSE(예산 집행)로 확정
-- - approval_request.status: PENDING(승인 대기)/APPROVED(승인 완료)/REJECTED(반려)로 확정
-- - notification.notification_type: APPROVAL_REQUEST(결재 요청)/NOTICE_CONFIRMATION(담당자 확인)/APPROVAL_RESULT(결재 결과)/
-- - ATTENDANCE_RISK(출결 주의)/CHAT_MENTION(채팅 멘션)/NOTICE(공지사항)/CALENDAR_EVENT(캘린더 변경)/CONSULTATION(상담)/SUBMISSION_DEADLINE(제출 마감)

-- ============================================================

START TRANSACTION;

-- ============================================================
-- 1. 공통 / 사용자
-- ============================================================

CREATE TABLE `users` (
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`name`	VARCHAR(50)	NOT NULL	COMMENT '이름',
	`phone`	VARCHAR(20)	NULL	COMMENT '전화번호',
	`email`	VARCHAR(255)	NOT NULL	COMMENT '이메일',
	`role`	ENUM('STUDENT','INSTRUCTOR','MANAGER')	NOT NULL	COMMENT '역할 (훈련생/강사/매니저)',
	`profile_img`	VARCHAR(500)	NULL	DEFAULT NULL	COMMENT '프로필이미지',
	`password`	VARCHAR(255)	NOT NULL	COMMENT '비밀번호',
	`need_reset_pw`	BOOLEAN	NOT NULL	DEFAULT TRUE	COMMENT '비밀번호재설정필요',
	`notification_on`	BOOLEAN	NOT NULL	DEFAULT TRUE	COMMENT '알림설정',
	`join_date`	DATE	NULL	COMMENT '입과일',
	`leave_date`	DATE	NULL	COMMENT '퇴과일',
	`status`	ENUM('ACTIVE','COMPLETED','WITHDRAWN','EXPELLED')	NOT NULL	DEFAULT 'ACTIVE'	COMMENT '상태 (재원/수료/자퇴/제적)'
);

CREATE TABLE `bootcamp_info` (
	`id`	BIGINT	NOT NULL,
	`org_name`	VARCHAR(255)	NULL	COMMENT '기관명',
	`pro_name`	VARCHAR(255)	NULL	COMMENT '과정명',
	`start_date`	DATE	NULL,
	`end_date`	DATE	NULL
);

CREATE TABLE `user_signature` (
	`signature_id`	BIGINT	NOT NULL,
	`signature_image_url`	VARCHAR(500)	NULL,
	`original_file_name`	VARCHAR(255)	NULL,
	`file_size_bytes`	BIGINT	NULL,
	`file_type`	VARCHAR(20)	NULL,
	`is_active`	BOOLEAN	NULL,
	`updated_at`	DATETIME	NULL,
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디'
);

CREATE TABLE `setting_change_log` (
	`setting_log_id`	BIGINT	NOT NULL	COMMENT '설정변경이력아이디',
	`changed_by`	BIGINT	NULL	COMMENT '변경자아이디',
	`changed_field`	VARCHAR(100)	NOT NULL	COMMENT '필드명',
	`old_value`	TEXT	NULL	COMMENT '이전값',
	`new_value`	TEXT	NULL	COMMENT '새값',
	`changed_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '변경일시'
);

-- ============================================================
-- 2. 팀 / 공간
-- ============================================================

CREATE TABLE `team` (
	`team_id`	BIGINT	NOT NULL	COMMENT '팀아이디',
	`name`	VARCHAR(100)	NOT NULL	COMMENT '이름',
	`sendbird_channel_url`	VARCHAR(255)	NULL	COMMENT '센드버드채널URL',
	`notion_page_id`	VARCHAR(100)	NULL	COMMENT '노션페이지아이디',
	`start_date`	DATE	NULL	COMMENT '시작일',
	`end_date`	DATE	NULL	COMMENT '종료일',
	`dissolved_at`	DATETIME	NULL	COMMENT '해체일시'
);

CREATE TABLE `team_member` (
	`team_member_id`	BIGINT	NOT NULL	COMMENT '팀원아이디',
	`team_id`	BIGINT	NOT NULL	COMMENT '팀아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`joined_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '가입일시',
	`left_at`	DATETIME	NULL	COMMENT '탈퇴일시'
);

-- 원본 space_eservation 오타 -> space_reservation으로 수정 완료
CREATE TABLE `space_reservation` (
	`space_id`	BIGINT	NOT NULL	COMMENT '공간아이디',
	`space_name`	VARCHAR(100)	NOT NULL	COMMENT '이름',
	`max_capacity`	INT	NULL	COMMENT '최대인원'
);

CREATE TABLE `trainee_location` (
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`space_id`	BIGINT	NULL	COMMENT '공간아이디'
);

-- ============================================================
-- 3. 출결
-- ============================================================

CREATE TABLE `attendance_policy` (
	`attendance_policy_id`	BIGINT	NOT NULL	COMMENT '출결정책아이디',
	`late_early_leave_conversion_count`	INT	NOT NULL	DEFAULT 3	COMMENT '지각조퇴외출환산횟수',
	`caution_threshold_pct`	DECIMAL(5, 2)	NULL	COMMENT '주의기준퍼센트',
	`warning_threshold_pct`	DECIMAL(5, 2)	NULL	COMMENT '경고기준퍼센트',
	`period_expulsion_pct`	DECIMAL(5, 2)	NOT NULL	DEFAULT 50.00	COMMENT '단위기간제적기준퍼센트',
	`total_expulsion_pct`	DECIMAL(5, 2)	NOT NULL	DEFAULT 20.00	COMMENT '전체제적기준퍼센트',
    `bootcamp_id`	BIGINT	NOT NULL,
     UNIQUE KEY `UQ_ATTENDANCE_POLICY_BOOTCAMP` (`bootcamp_id`)
);

CREATE TABLE `attendance_period` (
	`id`	BIGINT	NOT NULL,
	`period_no`	INT	NULL,
	`period_start`	DATE	NULL,
	`period_end`	DATE	NULL,
	`scheduled_class_days`	INT	NOT NULL,
	`bootcamp_id`	BIGINT	NOT NULL
);

CREATE TABLE `attendance` (
	`attendance_id`	BIGINT	NOT NULL	COMMENT '출결아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`attendance_date`	DATE	NOT NULL	COMMENT '출결일자',
	`status`	ENUM('PRESENT','LATE','EARLY_LEAVE','OUTING','ABSENT','LEAVE','SICK')	NOT NULL	COMMENT '상태',
	`check_in_time`	TIME	NULL	COMMENT '출근시간',
	`check_out_time`	TIME	NULL	COMMENT '퇴근시간',
	`external_ref_id`	VARCHAR(255)	NULL	COMMENT '외부참조아이디'
);

CREATE TABLE `attendance_period_summary` (
	`id`	BIGINT	NOT NULL,
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`present_days`	INT	NULL,
	`late_count`	INT	NULL,
	`early_leave_count`	INT	NULL,
	`absent_days`	INT	NULL,
	`outing_count`	INT	NULL,
	`leave_days`	INT	NULL,
	`sick_days`	INT	NULL,
	`attendance_rate`	DECIMAL(5, 2)	NULL,
	`risk_level`	ENUM('CAUTION','WARNING','RISK')	NULL	COMMENT '위험 수준',
    `period_id`	BIGINT	NOT NULL,
    UNIQUE KEY `UQ_PERIOD_SUMMARY_USER_PERIOD` (`user_id`, `period_id`)
);

CREATE TABLE `leave_balance` (
	`leave_balance_id`	BIGINT	NOT NULL	COMMENT '휴가잔액아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`period_start`	DATE	NOT NULL	COMMENT '기간시작일',
	`period_end`	DATE	NOT NULL	COMMENT '기간종료일',
	`total_days`	DECIMAL(4, 1)	NOT NULL	DEFAULT 0	COMMENT '총일수',
	`used_days`	DECIMAL(4, 1)	NOT NULL	DEFAULT 0	COMMENT '사용일수',
	`carried_over_days`	DECIMAL(4, 1)	NOT NULL	DEFAULT 0	COMMENT '이월일수'
);

CREATE TABLE `sick_balance` (
	`sick_balance_id`   BIGINT          NOT NULL,
	`user_id`           BIGINT          NOT NULL,
	`period_start`      DATE            NOT NULL,
	`period_end`        DATE            NOT NULL,
	`total_days`        DECIMAL(4, 1)   NOT NULL DEFAULT 0,
	`used_days`         DECIMAL(4, 1)   NOT NULL DEFAULT 0,
	`carried_over_days` DECIMAL(4, 1)   NOT NULL DEFAULT 0,
    UNIQUE KEY `UQ_SICK_BALANCE_USER_PERIOD` (`user_id`, `period_start`, `period_end`)
);

-- ============================================================
-- 4. 전자결재 / 예산
-- ============================================================

CREATE TABLE `budget_category` (
	`budget_category_id` BIGINT NOT NULL COMMENT '예산카테고리아이디',
	`name` VARCHAR(50) NOT NULL COMMENT '이름',
	`source` VARCHAR(10) NOT NULL DEFAULT 'SHEET' COMMENT '출처'
);

CREATE TABLE `budget_allocation` (
	`budget_allocation_id` BIGINT NOT NULL COMMENT '예산배정아이디',
	`budget_category_id` BIGINT NOT NULL COMMENT '예산카테고리아이디',
	`total_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '총액',
	`used_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '사용액',
	`period_start` DATE NULL COMMENT '기간시작일',
	`period_end` DATE NULL COMMENT '기간종료일',
	`last_synced_at` DATETIME NULL COMMENT '마지막동기화일시'
);

CREATE TABLE `approval_request` (
	`approval_id` BIGINT NOT NULL COMMENT '결재요청아이디',
	`requester_id` BIGINT NOT NULL COMMENT '신청자아이디',
	`approver_id` BIGINT NULL COMMENT '승인자아이디',
	`request_type` ENUM('LEAVE','PURCHASE') NOT NULL COMMENT '요청유형 (휴가/구매요청)',
	`status` ENUM('PENDING','CHECKED','APPROVED','REJECTED','COMPLETED')
    NOT NULL DEFAULT 'PENDING'
    COMMENT '상태',
	`title` VARCHAR(100) NOT NULL COMMENT '제목',
	`reason` TEXT NULL COMMENT '신청사유',
	`rejection_reason` TEXT NULL COMMENT '반려사유',
	`requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신청일시',
	`confirmed_at` DATETIME NULL COMMENT '확인일시',
	`processed_at` DATETIME NULL COMMENT '처리일시',
	`signature_id` BIGINT NULL COMMENT '전자서명아이디'
);

CREATE TABLE `approval_leave_detail` (
	`leave_detail_id` BIGINT NOT NULL COMMENT '휴가상세아이디',
	`approval_id` BIGINT NOT NULL COMMENT '결재요청아이디',
	`start_date` DATE NOT NULL COMMENT '시작일',
	`end_date` DATE NOT NULL COMMENT '종료일',
	`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
	`updated_at` DATETIME NULL COMMENT '수정일시'
);

CREATE TABLE `approval_purchase_detail` (
	`purchase_detail_id` BIGINT NOT NULL COMMENT '구매상세아이디',
	`approval_id` BIGINT NOT NULL COMMENT '결재요청아이디',
	`budget_category_id` BIGINT NOT NULL COMMENT '예산카테고리아이디',
	`item_name` VARCHAR(100) NOT NULL COMMENT '구매품목명',
	`amount` DECIMAL(14, 2) NOT NULL COMMENT '요청금액',
	`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
	`updated_at` DATETIME NULL COMMENT '수정일시'
);

CREATE TABLE `approval_history` (
	`approval_history_id` BIGINT NOT NULL COMMENT '결재이력아이디',
	`approval_id` BIGINT NOT NULL COMMENT '결재요청아이디',
	`changed_by` BIGINT NULL COMMENT '변경자아이디',
	`field_name` VARCHAR(30) NOT NULL COMMENT '필드명',
	`old_value` VARCHAR(30) NULL COMMENT '이전값',
	`new_value` VARCHAR(30) NULL COMMENT '새값',
	`note` TEXT NULL COMMENT '비고',
	`changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변경일시'
);

CREATE TABLE `approval_attachment` (
	`attachment_id` BIGINT NOT NULL COMMENT '첨부파일아이디',
	`approval_id` BIGINT NOT NULL COMMENT '결재요청아이디',
	`file_url` VARCHAR(500) NOT NULL COMMENT '파일URL',
	`file_name` VARCHAR(255) NULL COMMENT '파일명',
	`uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드일시'
);

-- ============================================================
-- 5. 채팅
-- ============================================================

CREATE TABLE `chat_channel` (
	`chat_channel_id`	BIGINT	NOT NULL	COMMENT '채팅채널아이디',
	`sendbird_channel_url`	VARCHAR(255)	NOT NULL	COMMENT '센드버드채널URL',
	`channel_type`	ENUM('DM','GROUP')	NOT NULL	COMMENT '채널유형',
	`name`	VARCHAR(255)	NULL	COMMENT '이름',
	`team_id`	BIGINT	NULL	COMMENT '팀아이디'
);

CREATE TABLE `chat_channel_member` (
	`chat_channel_member_id`	BIGINT	NOT NULL	COMMENT '채팅채널참여자아이디',
	`chat_channel_id`	BIGINT	NOT NULL	COMMENT '채팅채널아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`joined_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '참여일시',
	`left_at`	DATETIME	NULL	COMMENT '퇴장일시',
	`last_read_message_id`	BIGINT	NULL	COMMENT '마지막읽은메시지아이디',
	`last_read_at`	DATETIME	NULL	COMMENT '마지막읽은일시'
);

CREATE TABLE `chat_message_mirror` (
	`chat_message_id`	BIGINT	NOT NULL	COMMENT '채팅메시지아이디',
	`channel_id`	VARCHAR(100)	NOT NULL	COMMENT '채널아이디',
	`sendbird_message_id`	VARCHAR(100)	NOT NULL	COMMENT '센드버드메시지아이디',
	`parent_message_id`	BIGINT	NULL	COMMENT '상위메시지아이디',
	`sender_id`	BIGINT	NULL	COMMENT '발신자아이디',
	`content`	TEXT	NULL	COMMENT '내용',
	`attachment_url`	VARCHAR(500)	NULL	COMMENT '첨부파일URL',
	`attachment_type`	VARCHAR(30)	NULL	COMMENT '첨부파일유형',
	`sent_at`	DATETIME	NOT NULL	COMMENT '전송일시'
);

CREATE TABLE `chat_mention` (
	`mention_id`	BIGINT	NOT NULL	COMMENT '멘션아이디',
	`chat_message_id`	BIGINT	NOT NULL	COMMENT '채팅메시지아이디',
	`mentioned_user_id`	BIGINT	NOT NULL	COMMENT '언급된사용자아이디'
);

-- ============================================================
-- 6. 알림 / AI 에이전트
-- ============================================================

CREATE TABLE `notification` (
	`notification_id`	BIGINT	NOT NULL	COMMENT '알림아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`notification_type`	ENUM('NOTICE','APPROVAL_REQUEST','NOTICE_CONFIRMATION','APPROVAL_RESULT','CONSULTATION_CANCEL','SUBMISSION_DEADLINE','CALENDAR_EVENT','ATTENDANCE_RISK','CHAT_MENTION')	NOT NULL	COMMENT '알림유형 (NOTI-006 명세 기준: 공지등록/결재요청/담당자확인/승인반려/상담예약취소/제출마감/일정등록/출결위험 + 채팅멘션)',
	`related_entity_type`	VARCHAR(30)	NULL	COMMENT '관련엔티티유형',
	`related_entity_id`	BIGINT	NULL	COMMENT '관련엔티티아이디 -- 폴리모픽 참조, FK 미설정',
	`content`	TEXT	NOT NULL	COMMENT '내용',
	`is_read`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '읽음여부',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '수신일시 (최신순 정렬 기준)'
);

CREATE TABLE `ai_agent_log` (
	`agent_log_id`	BIGINT	NOT NULL	COMMENT '에이전트로그아이디',
	`action_type`	VARCHAR(50)	NOT NULL	COMMENT '행동유형',
	`target_entity_type`	VARCHAR(30)	NOT NULL	COMMENT '대상엔티티유형',
	`target_entity_id`	BIGINT	NOT NULL	COMMENT '대상엔티티아이디 -- 폴리모픽 참조, FK 미설정',
	`tool_calls`	JSON	NULL	COMMENT '도구호출내역',
	`ai_reasoning`	TEXT	NULL	COMMENT 'AI판단근거',
	`executed_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '실행일시'
);

-- ============================================================
-- 7. 공지사항 / 캘린더
-- ============================================================

CREATE TABLE `notice_category` (
	`notice_category_id`	BIGINT	NOT NULL	COMMENT '공지카테고리아이디',
	`name`	VARCHAR(50)	NOT NULL	COMMENT '이름',
	`is_default`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '기본여부'
);

CREATE TABLE `notice` (
	`notice_id`	BIGINT	NOT NULL	COMMENT '공지사항아이디',
	`author_id`	BIGINT	NOT NULL	COMMENT '작성자아이디',
	`notice_category_id`	BIGINT	NOT NULL	COMMENT '공지카테고리아이디',
	`title`	VARCHAR(255)	NOT NULL	COMMENT '제목',
	`content`	TEXT	NOT NULL	COMMENT '내용',
	`is_mandatory`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '필수여부',
	`confirmation_type`	VARCHAR(20)	NULL	COMMENT '확인방식',
	`is_visible_to_trainee`	BOOLEAN	NOT NULL	DEFAULT TRUE	COMMENT '훈련생공개여부'
);

CREATE TABLE `notice_attachment` (
	`notice_attachment_id`	BIGINT	NOT NULL	COMMENT '공지첨부파일아이디',
	`notice_id`	BIGINT	NOT NULL	COMMENT '공지사항아이디',
	`file_url`	VARCHAR(500)	NOT NULL	COMMENT '파일URL',
	`file_name`	VARCHAR(255)	NULL	COMMENT '파일명',
	`file_size_bytes`	BIGINT	NULL	COMMENT '파일크기',
	`file_type`	VARCHAR(20)	NULL	COMMENT '파일유형',
	`uploaded_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '업로드일시'
);

CREATE TABLE `notice_confirmation` (
	`notice_id`	BIGINT	NOT NULL	COMMENT '공지사항아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`reaction_emoji`	VARCHAR(10)	NULL	COMMENT '반응이모지',
	`confirmed_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '확인일시'
);

CREATE TABLE `calendar_event` (
	`calendar_event_id`	BIGINT	NOT NULL	COMMENT '캘린더일정아이디',
	`google_event_id`	VARCHAR(255)	NULL	COMMENT '구글이벤트아이디',
	`team_id`	BIGINT	NULL	COMMENT '팀아이디',
	`title`	VARCHAR(255)	NOT NULL	COMMENT '제목',
	`location`	VARCHAR(255)	NULL	COMMENT '장소',
	`event_type`	VARCHAR(20)	NOT NULL	COMMENT '일정유형',
	`start_time`	DATETIME	NOT NULL	COMMENT '시작일시',
	`end_time`	DATETIME	NOT NULL	COMMENT '종료일시',
	`is_auto_registered`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '자동등록여부',
	`ai_extracted`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT 'AI추출여부',
	`created_by`	BIGINT	NULL	COMMENT '생성자아이디'
);

CREATE TABLE `notice_ai_event_candidate` (
	`candidate_id`	BIGINT	NOT NULL	COMMENT '후보아이디',
	`notice_id`	BIGINT	NOT NULL	COMMENT '공지사항아이디',
	`title`	VARCHAR(255)	NULL	COMMENT '제목',
	`event_date`	DATE	NULL	COMMENT '일정일자',
	`event_time`	TIME	NULL	COMMENT '일정시간',
	`location`	VARCHAR(255)	NULL	COMMENT '장소',
	`is_ambiguous`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '모호여부',
	`status`	VARCHAR(20)	NOT NULL	DEFAULT 'PENDING'	COMMENT '상태',
	`reviewed_by`	BIGINT	NULL	COMMENT '검토자아이디',
	`reviewed_at`	DATETIME	NULL	COMMENT '검토일시',
	`resulting_calendar_event_id`	BIGINT	NULL	COMMENT '생성된캘린더일정아이디'
);

-- ============================================================
-- 8. 상담
-- ============================================================

CREATE TABLE `consultation` (
	`consultation_id`	BIGINT	NOT NULL	COMMENT '상담아이디',
	`counselor_id`	BIGINT	NULL	COMMENT '상담자아이디',
	`requester_id`	BIGINT	NOT NULL	COMMENT '신청자아이디',
	`topic`	VARCHAR(255)	NULL	COMMENT '주제',
	`content`	TEXT	NULL	COMMENT '내용',
	`ai_brief`	TEXT	NULL	COMMENT 'AI요약',
	`status`	ENUM('PENDING','CHECKED','APPROVED','CANCELLED','COMPLETED')	NOT NULL	DEFAULT 'PENDING'	COMMENT '상태',
	`scheduled_at`	DATETIME	NULL	COMMENT '예정일시',
	`external_ref_id`	VARCHAR(255)	NULL	COMMENT '외부참조아이디',
	`cancelled_by`	VARCHAR(20)	NULL	COMMENT '취소주체',
	`cancel_reason`	TEXT	NULL	COMMENT '취소사유'
);

CREATE TABLE `counselor_calendar` (
	`id`	BIGINT	NOT NULL	COMMENT '상담가능시간아이디',
	`counselor_id`	BIGINT	NOT NULL	COMMENT '상담자아이디',
    `calendar_code`	VARCHAR(255)	NOT NULL,
    UNIQUE KEY `UQ_COUNSELOR_CALENDAR_COUNSELOR` (`counselor_id`),
    UNIQUE KEY `UQ_COUNSELOR_CALENDAR_CODE` (`calendar_code`)
);

-- ============================================================
-- 9. 제출물 / 평가 / 시트연동
-- ============================================================

CREATE TABLE `survey_form` (
	`survey_form_id`	BIGINT	NOT NULL	COMMENT '설문폼아이디',
	`form_type`	VARCHAR(20)	NOT NULL	COMMENT '설문유형',
	`google_form_id`	VARCHAR(255)	NOT NULL	COMMENT '구글폼아이디',
	`created_by`	BIGINT	NULL	COMMENT '생성자아이디'
);

CREATE TABLE `submission_box` (
	`submission_box_id`	BIGINT	NOT NULL	COMMENT '제출함아이디',
	`project_name`	VARCHAR(255)	NOT NULL	COMMENT '프로젝트명',
	`target_scope`	VARCHAR(20)	NOT NULL	COMMENT '제출대상범위',
	`start_date`	DATE	NULL	COMMENT '시작일',
	`due_at`	DATETIME	NOT NULL	COMMENT '마감일시',
	`allowed_file_types`	VARCHAR(255)	NULL	COMMENT '허용파일형식',
	`late_policy`	VARCHAR(20)	NOT NULL	DEFAULT 'BLOCK'	COMMENT '지각제출정책',
	`created_by`	BIGINT	NULL	COMMENT '생성자아이디'
);

CREATE TABLE `submission` (
	`submission_id`	BIGINT	NOT NULL	COMMENT '제출물아이디',
	`submission_box_id`	BIGINT	NOT NULL	COMMENT '제출함아이디',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자아이디',
	`team_id`	BIGINT	NULL	COMMENT '팀아이디',
	`file_url`	VARCHAR(500)	NULL	COMMENT '파일URL',
	`submitted_at`	DATETIME	NULL	COMMENT '제출일시',
	`is_late`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '지각여부'
);

CREATE TABLE `external_sheet_link` (
	`sheet_link_id`	BIGINT	NOT NULL	COMMENT '시트연동아이디',
	`domain`	VARCHAR(20)	NOT NULL	COMMENT '도메인',
	`sheet_url`	VARCHAR(500)	NOT NULL	COMMENT '시트URL',
	`tab_name`	VARCHAR(100)	NULL	COMMENT '탭명',
	`column_mapping`	JSON	NULL	COMMENT '컬럼매핑',
	`last_synced_at`	DATETIME	NULL	COMMENT '마지막동기화일시'
);

CREATE TABLE `sheet_sync_log` (
	`sync_log_id`	BIGINT	NOT NULL	COMMENT '동기화이력아이디',
	`sheet_link_id`	BIGINT	NOT NULL	COMMENT '시트연동아이디',
	`changed_range`	VARCHAR(100)	NULL	COMMENT '변경범위',
	`diff_summary`	TEXT	NULL	COMMENT '변경요약',
	`synced_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '동기화일시'
);

CREATE TABLE `evaluation_record` (
	`evaluation_record_id`	BIGINT	NOT NULL	COMMENT '평가기록아이디',
	`trainee_id`	BIGINT	NOT NULL	COMMENT '훈련생아이디',
	`sheet_link_id`	BIGINT	NOT NULL	COMMENT '시트연동아이디',
	`evaluation_type`	VARCHAR(50)	NULL	COMMENT '평가유형',
	`item`	VARCHAR(255)	NULL	COMMENT '항목',
	`score`	DECIMAL(5, 2)	NULL	COMMENT '점수',
	`comment`	TEXT	NULL	COMMENT '의견',
	`sheet_row_key`	VARCHAR(255)	NOT NULL	COMMENT '시트행식별자',
	`synced_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '동기화일시'
);

-- ============================================================
-- PK 제약조건
-- ============================================================

ALTER TABLE `team` ADD CONSTRAINT `PK_TEAM` PRIMARY KEY (`team_id`);
ALTER TABLE `notification` ADD CONSTRAINT `PK_NOTIFICATION` PRIMARY KEY (`notification_id`);
ALTER TABLE `survey_form` ADD CONSTRAINT `PK_SURVEY_FORM` PRIMARY KEY (`survey_form_id`);
ALTER TABLE `ai_agent_log` ADD CONSTRAINT `PK_AI_AGENT_LOG` PRIMARY KEY (`agent_log_id`);
ALTER TABLE `chat_channel_member` ADD CONSTRAINT `PK_CHAT_CHANNEL_MEMBER` PRIMARY KEY (`chat_channel_member_id`);
ALTER TABLE `attendance_policy` ADD CONSTRAINT `PK_ATTENDANCE_POLICY` PRIMARY KEY (`attendance_policy_id`);
ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (`user_id`);
ALTER TABLE `notice` ADD CONSTRAINT `PK_NOTICE` PRIMARY KEY (`notice_id`);
ALTER TABLE `notice_attachment` ADD CONSTRAINT `PK_NOTICE_ATTACHMENT` PRIMARY KEY (`notice_attachment_id`);
ALTER TABLE `trainee_location` ADD CONSTRAINT `PK_TRAINEE_LOCATION` PRIMARY KEY (`user_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `PK_CALENDAR_EVENT` PRIMARY KEY (`calendar_event_id`);
ALTER TABLE `consultation` ADD CONSTRAINT `PK_CONSULTATION` PRIMARY KEY (`consultation_id`);
ALTER TABLE `notice_confirmation` ADD CONSTRAINT `PK_NOTICE_CONFIRMATION` PRIMARY KEY (`notice_id`, `user_id`);
ALTER TABLE `submission` ADD CONSTRAINT `PK_SUBMISSION` PRIMARY KEY (`submission_id`);
ALTER TABLE `notice_category` ADD CONSTRAINT `PK_NOTICE_CATEGORY` PRIMARY KEY (`notice_category_id`);
ALTER TABLE `sheet_sync_log` ADD CONSTRAINT `PK_SHEET_SYNC_LOG` PRIMARY KEY (`sync_log_id`);
ALTER TABLE `approval_history` ADD CONSTRAINT `PK_APPROVAL_HISTORY` PRIMARY KEY (`approval_history_id`);
ALTER TABLE `external_sheet_link` ADD CONSTRAINT `PK_EXTERNAL_SHEET_LINK` PRIMARY KEY (`sheet_link_id`);
ALTER TABLE `submission_box` ADD CONSTRAINT `PK_SUBMISSION_BOX` PRIMARY KEY (`submission_box_id`);
ALTER TABLE `approval_attachment` ADD CONSTRAINT `PK_APPROVAL_ATTACHMENT` PRIMARY KEY (`attachment_id`);
ALTER TABLE `approval_leave_detail` ADD CONSTRAINT `PK_APPROVAL_LEAVE_DETAIL` PRIMARY KEY (`leave_detail_id`);
ALTER TABLE `approval_purchase_detail` ADD CONSTRAINT `PK_APPROVAL_PURCHASE_DETAIL` PRIMARY KEY (`purchase_detail_id`);
ALTER TABLE `attendance` ADD CONSTRAINT `PK_ATTENDANCE` PRIMARY KEY (`attendance_id`);
ALTER TABLE `leave_balance` ADD CONSTRAINT `PK_LEAVE_BALANCE` PRIMARY KEY (`leave_balance_id`);
ALTER TABLE `sick_balance` ADD CONSTRAINT `PK_SICK_BALANCE` PRIMARY KEY (`sick_balance_id`);
ALTER TABLE `budget_allocation` ADD CONSTRAINT `PK_BUDGET_ALLOCATION` PRIMARY KEY (`budget_allocation_id`);
ALTER TABLE `chat_message_mirror` ADD CONSTRAINT `PK_CHAT_MESSAGE_MIRROR` PRIMARY KEY (`chat_message_id`);
ALTER TABLE `team_member` ADD CONSTRAINT `PK_TEAM_MEMBER` PRIMARY KEY (`team_member_id`);
ALTER TABLE `evaluation_record` ADD CONSTRAINT `PK_EVALUATION_RECORD` PRIMARY KEY (`evaluation_record_id`);
ALTER TABLE `chat_channel` ADD CONSTRAINT `PK_CHAT_CHANNEL` PRIMARY KEY (`chat_channel_id`);
ALTER TABLE `budget_category` ADD CONSTRAINT `PK_BUDGET_CATEGORY` PRIMARY KEY (`budget_category_id`);
ALTER TABLE `setting_change_log` ADD CONSTRAINT `PK_SETTING_CHANGE_LOG` PRIMARY KEY (`setting_log_id`);
ALTER TABLE `approval_request` ADD CONSTRAINT `PK_APPROVAL_REQUEST` PRIMARY KEY (`approval_id`);
ALTER TABLE `chat_mention` ADD CONSTRAINT `PK_CHAT_MENTION` PRIMARY KEY (`mention_id`);
ALTER TABLE `space_reservation` ADD CONSTRAINT `PK_SPACE_RESERVATION` PRIMARY KEY (`space_id`);
ALTER TABLE `counselor_calendar` ADD CONSTRAINT `PK_COUNSELOR_CALENDAR` PRIMARY KEY (`id`);
ALTER TABLE `notice_ai_event_candidate` ADD CONSTRAINT `PK_NOTICE_AI_EVENT_CANDIDATE` PRIMARY KEY (`candidate_id`);
ALTER TABLE `attendance_period_summary` ADD CONSTRAINT `PK_ATTENDANCE_PERIOD_SUMMARY` PRIMARY KEY (`id`);
ALTER TABLE `bootcamp_info` ADD CONSTRAINT `PK_BOOTCAMP_INFO` PRIMARY KEY (`id`);
ALTER TABLE `user_signature` ADD CONSTRAINT `PK_USER_SIGNATURE` PRIMARY KEY (`signature_id`);
ALTER TABLE `attendance_period` ADD CONSTRAINT `PK_ATTENDANCE_PERIOD` PRIMARY KEY (`id`);

-- ============================================================
-- FK 제약조건 (원본 4개 + 신규 보강분)
-- ============================================================

-- 원본 존재분
ALTER TABLE `trainee_location` ADD CONSTRAINT `FK_users_TO_trainee_location_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
ALTER TABLE `notice_confirmation` ADD CONSTRAINT `FK_notice_TO_notice_confirmation_1` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`) ON DELETE CASCADE;
ALTER TABLE `notice_confirmation` ADD CONSTRAINT `FK_users_TO_notice_confirmation_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `attendance_period_summary` ADD CONSTRAINT `FK_users_TO_attendance_period_summary_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

-- 신규 보강분
ALTER TABLE `user_signature` ADD CONSTRAINT `FK_users_TO_user_signature_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
ALTER TABLE `setting_change_log` ADD CONSTRAINT `FK_users_TO_setting_change_log_1` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`);

ALTER TABLE `team_member` ADD CONSTRAINT `FK_team_TO_team_member_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`) ON DELETE CASCADE;
ALTER TABLE `team_member` ADD CONSTRAINT `FK_users_TO_team_member_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `trainee_location` ADD CONSTRAINT `FK_space_reservation_TO_trainee_location_1` FOREIGN KEY (`space_id`) REFERENCES `space_reservation` (`space_id`);

ALTER TABLE `attendance_policy` ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_policy_1` FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);
ALTER TABLE `attendance_period` ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_period_1` FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);
ALTER TABLE `attendance` ADD CONSTRAINT `FK_users_TO_attendance_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `attendance_period_summary` ADD CONSTRAINT `FK_attendance_period_TO_attendance_period_summary_1` FOREIGN KEY (`period_id`) REFERENCES `attendance_period` (`id`) ON DELETE CASCADE;
ALTER TABLE `leave_balance` ADD CONSTRAINT `FK_users_TO_leave_balance_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `sick_balance` ADD CONSTRAINT `FK_users_TO_sick_balance_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `budget_allocation` ADD CONSTRAINT `FK_budget_category_TO_budget_allocation_1` FOREIGN KEY (`budget_category_id`) REFERENCES `budget_category` (`budget_category_id`);
ALTER TABLE `approval_request` ADD CONSTRAINT `FK_users_TO_approval_request_1` FOREIGN KEY (`requester_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `approval_request` ADD CONSTRAINT `FK_users_TO_approval_request_2` FOREIGN KEY (`approver_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `approval_request` ADD CONSTRAINT `FK_user_signature_TO_approval_request_1` FOREIGN KEY (`signature_id`) REFERENCES `user_signature` (`signature_id`);
ALTER TABLE `approval_history` ADD CONSTRAINT `FK_approval_request_TO_approval_history_1` FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`) ON DELETE CASCADE;
ALTER TABLE `approval_history` ADD CONSTRAINT `FK_users_TO_approval_history_1` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;
ALTER TABLE `approval_attachment` ADD CONSTRAINT `FK_approval_request_TO_approval_attachment_1` FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`) ON DELETE CASCADE;
ALTER TABLE `approval_leave_detail` ADD CONSTRAINT `FK_approval_request_TO_approval_leave_detail_1` FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`) ON DELETE CASCADE;
ALTER TABLE `approval_purchase_detail` ADD CONSTRAINT `FK_approval_request_TO_approval_purchase_detail_1` FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`) ON DELETE CASCADE;
ALTER TABLE `approval_purchase_detail` ADD CONSTRAINT `FK_budget_category_TO_approval_purchase_detail_1` FOREIGN KEY (`budget_category_id`) REFERENCES `budget_category` (`budget_category_id`);

ALTER TABLE `chat_channel` ADD CONSTRAINT `FK_team_TO_chat_channel_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`) ON DELETE CASCADE;
ALTER TABLE `chat_channel_member` ADD CONSTRAINT `FK_chat_channel_TO_chat_channel_member_1` FOREIGN KEY (`chat_channel_id`) REFERENCES `chat_channel` (`chat_channel_id`) ON DELETE CASCADE;
ALTER TABLE `chat_channel_member` ADD CONSTRAINT `FK_users_TO_chat_channel_member_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `chat_channel_member` ADD CONSTRAINT `FK_chat_message_mirror_TO_chat_channel_member_1` FOREIGN KEY (`last_read_message_id`) REFERENCES `chat_message_mirror` (`chat_message_id`);
ALTER TABLE `chat_message_mirror` ADD CONSTRAINT `FK_users_TO_chat_message_mirror_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;
ALTER TABLE `chat_message_mirror` ADD CONSTRAINT `FK_chat_message_mirror_TO_chat_message_mirror_1` FOREIGN KEY (`parent_message_id`) REFERENCES `chat_message_mirror` (`chat_message_id`);
ALTER TABLE `chat_mention` ADD CONSTRAINT `FK_chat_message_mirror_TO_chat_mention_1` FOREIGN KEY (`chat_message_id`) REFERENCES `chat_message_mirror` (`chat_message_id`) ON DELETE CASCADE;
ALTER TABLE `chat_mention` ADD CONSTRAINT `FK_users_TO_chat_mention_1` FOREIGN KEY (`mentioned_user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `notification` ADD CONSTRAINT `FK_users_TO_notification_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `notice` ADD CONSTRAINT `FK_users_TO_notice_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `notice` ADD CONSTRAINT `FK_notice_category_TO_notice_1` FOREIGN KEY (`notice_category_id`) REFERENCES `notice_category` (`notice_category_id`);
ALTER TABLE `notice_attachment` ADD CONSTRAINT `FK_notice_TO_notice_attachment_1` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`) ON DELETE CASCADE;
ALTER TABLE `notice_ai_event_candidate` ADD CONSTRAINT `FK_notice_TO_notice_ai_event_candidate_1` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`) ON DELETE CASCADE;
ALTER TABLE `notice_ai_event_candidate` ADD CONSTRAINT `FK_users_TO_notice_ai_event_candidate_1` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;
ALTER TABLE `notice_ai_event_candidate` ADD CONSTRAINT `FK_calendar_event_TO_notice_ai_event_candidate_1` FOREIGN KEY (`resulting_calendar_event_id`) REFERENCES `calendar_event` (`calendar_event_id`) ON DELETE SET NULL;
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_team_TO_calendar_event_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`) ON DELETE SET NULL;
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_users_TO_calendar_event_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

ALTER TABLE `consultation` ADD CONSTRAINT `FK_users_TO_consultation_1` FOREIGN KEY (`counselor_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;
ALTER TABLE `consultation` ADD CONSTRAINT `FK_users_TO_consultation_2` FOREIGN KEY (`requester_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `counselor_calendar` ADD CONSTRAINT `FK_users_TO_counselor_calendar_1` FOREIGN KEY (`counselor_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `survey_form` ADD CONSTRAINT `FK_users_TO_survey_form_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`);
ALTER TABLE `submission_box` ADD CONSTRAINT `FK_users_TO_submission_box_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`);
ALTER TABLE `submission` ADD CONSTRAINT `FK_submission_box_TO_submission_1` FOREIGN KEY (`submission_box_id`) REFERENCES `submission_box` (`submission_box_id`);
ALTER TABLE `submission` ADD CONSTRAINT `FK_users_TO_submission_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `submission` ADD CONSTRAINT `FK_team_TO_submission_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`) ON DELETE SET NULL;

ALTER TABLE `sheet_sync_log` ADD CONSTRAINT `FK_external_sheet_link_TO_sheet_sync_log_1` FOREIGN KEY (`sheet_link_id`) REFERENCES `external_sheet_link` (`sheet_link_id`) ON DELETE CASCADE;
ALTER TABLE `evaluation_record` ADD CONSTRAINT `FK_users_TO_evaluation_record_1` FOREIGN KEY (`trainee_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `evaluation_record` ADD CONSTRAINT `FK_external_sheet_link_TO_evaluation_record_1` FOREIGN KEY (`sheet_link_id`) REFERENCES `external_sheet_link` (`sheet_link_id`);

-- ============================================================
-- 인덱스
-- ============================================================

ALTER TABLE `attendance` ADD CONSTRAINT `UQ_ATTENDANCE_USER_DATE` UNIQUE (`user_id`, `attendance_date`);
ALTER TABLE `chat_channel_member` ADD CONSTRAINT `UQ_CHAT_CHANNEL_MEMBER` UNIQUE (`chat_channel_id`, `user_id`);
ALTER TABLE `team_member` ADD CONSTRAINT `UQ_TEAM_MEMBER` UNIQUE (`team_id`, `user_id`);
ALTER TABLE `submission` ADD CONSTRAINT `UQ_SUBMISSION_BOX_USER` UNIQUE (`submission_box_id`, `user_id`);

CREATE INDEX `IDX_CHAT_MESSAGE_MIRROR_CHANNEL_SENT` ON `chat_message_mirror` (`channel_id`, `sent_at`);
CREATE INDEX `IDX_APPROVAL_REQUEST_REQUESTER_STATUS` ON `approval_request` (`requester_id`, `status`);
CREATE INDEX `IDX_APPROVAL_REQUEST_APPROVER_STATUS` ON `approval_request` (`approver_id`, `status`);
CREATE INDEX `IDX_NOTIFICATION_USER_READ` ON `notification` (`user_id`, `is_read`);

COMMIT;
