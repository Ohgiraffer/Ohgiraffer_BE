-- ============================================================
-- leave_balance: (user_id, period_start) 복합 유니크 추가
-- ============================================================
ALTER TABLE leave_balance
    ADD CONSTRAINT UQ_leave_balance_user_period UNIQUE (user_id, period_start);

-- ============================================================
-- sick_balance: 3컬럼 유니크 -> 2컬럼 유니크로 교체
-- FK_users_TO_sick_balance_1이 UQ_SICK_BALANCE_USER_PERIOD를
-- 커버 인덱스로 사용 중이므로 FK 해제 -> 인덱스 교체 -> FK 재생성 순서로 분리
-- ============================================================

-- 1. FK 해제
ALTER TABLE sick_balance
DROP FOREIGN KEY FK_users_TO_sick_balance_1;

-- 2. 기존 3컬럼 유니크 인덱스 제거
ALTER TABLE sick_balance
DROP INDEX UQ_SICK_BALANCE_USER_PERIOD;

-- 3. 신규 2컬럼 유니크 인덱스 추가
ALTER TABLE sick_balance
    ADD CONSTRAINT UQ_sick_balance_user_period UNIQUE (user_id, period_start);

-- 4. FK 재생성
ALTER TABLE sick_balance
    ADD CONSTRAINT FK_users_TO_sick_balance_1
        FOREIGN KEY (user_id) REFERENCES users (user_id);