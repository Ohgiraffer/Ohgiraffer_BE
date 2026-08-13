ALTER TABLE `sheet_sync_log`
    ADD CONSTRAINT `FK_users_TO_sheet_sync_log_executor_id`
        FOREIGN KEY (`executor_id`) REFERENCES `users` (`user_id`)
            ON DELETE SET NULL;