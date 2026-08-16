DROP TABLE IF EXISTS leave_balance;
DROP TABLE IF EXISTS sick_balance;

CREATE TABLE leave_balance (
                               leave_balance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               total_days DECIMAL(5,1) NOT NULL DEFAULT 0,
                               used_days DECIMAL(5,1) NOT NULL DEFAULT 0,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT UQ_leave_balance_user UNIQUE (user_id),
                               CONSTRAINT FK_users_TO_leave_balance_1
                                   FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE sick_balance (
                              sick_balance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              total_days DECIMAL(5,1) NOT NULL DEFAULT 0,
                              used_days DECIMAL(5,1) NOT NULL DEFAULT 0,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT UQ_sick_balance_user UNIQUE (user_id),
                              CONSTRAINT FK_users_TO_sick_balance_1
                                  FOREIGN KEY (user_id) REFERENCES users (user_id)
);