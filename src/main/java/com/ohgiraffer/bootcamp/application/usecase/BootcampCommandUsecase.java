package com.ohgiraffer.bootcamp.application.usecase;

import java.time.LocalDate;

public interface BootcampCommandUsecase {
    Long register(String orgName, String proName, LocalDate startDate, LocalDate endDate);
    void update(Long bootcampId, String orgName, String proName, LocalDate startDate, LocalDate endDate);
}
