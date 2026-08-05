package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.presentation.api.request.BootcampPolicyRequest;

import java.time.LocalDate;

public interface BootcampCommandUsecase {
    Long register(String orgName, String proName, LocalDate startDate, LocalDate endDate);
    void update(Long bootcampId, String orgName, String proName, LocalDate startDate, LocalDate endDate);

    void savePolicy(BootcampPolicyRequest request);
}
