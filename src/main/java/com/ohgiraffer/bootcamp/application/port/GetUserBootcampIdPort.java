package com.ohgiraffer.bootcamp.application.port;

import java.util.Optional;

public interface GetUserBootcampIdPort {
    Optional<Long> findBootcampIdByUserId(Long userId);

}
