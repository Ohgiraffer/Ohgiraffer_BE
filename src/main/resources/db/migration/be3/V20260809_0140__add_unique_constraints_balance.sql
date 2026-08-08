ALTER TABLE leave_balance
    ADD CONSTRAINT UQ_leave_balance_user_period UNIQUE (user_id, period_start);

ALTER TABLE sick_balance
DROP FOREIGN KEY FK_users_TO_sick_balance_1;

ALTER TABLE sick_balance
DROP INDEX UQ_SICK_BALANCE_USER_PERIOD;

ALTER TABLE sick_balance
    ADD CONSTRAINT UQ_sick_balance_user_period UNIQUE (user_id, period_start);

ALTER TABLE sick_balance
    ADD CONSTRAINT FK_users_TO_sick_balance_1
        FOREIGN KEY (user_id) REFERENCES users (user_id);