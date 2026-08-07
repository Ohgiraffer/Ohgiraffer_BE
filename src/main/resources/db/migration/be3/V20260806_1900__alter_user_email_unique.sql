ALTER TABLE `users`
    ADD CONSTRAINT `UQ_users_email` UNIQUE (`email`);