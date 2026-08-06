package com.ohgiraffer.bootcamp.application.port;

public interface SetBootcampIdPort {
    boolean assignBootcampIfAbsent(Long userId, Long bootcampId);
}
