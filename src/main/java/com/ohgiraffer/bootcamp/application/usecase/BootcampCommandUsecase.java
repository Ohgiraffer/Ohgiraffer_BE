package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.application.command.PeriodCommand;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampPolicyRequest;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public interface BootcampCommandUsecase {
    Long register(Long userId, String orgName, String proName, LocalDate startDate, LocalDate endDate);
    void update(Long bootcampId, String orgName, String proName, LocalDate startDate, LocalDate endDate);

    void savePolicy(BootcampPolicyRequest request);

    void updateSettings(Long userId, String orgName, String proName,
                        LocalDate startDate, LocalDate endDate,
                        List<PeriodCommand> periods);
}