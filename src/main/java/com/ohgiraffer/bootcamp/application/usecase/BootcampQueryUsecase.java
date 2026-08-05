package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;

public interface BootcampQueryUsecase {
    BootcampSettingsResponse getSettings(Long userId);
}
