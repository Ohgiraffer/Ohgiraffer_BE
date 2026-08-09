package com.ohgiraffer.space.application.service;

import com.ohgiraffer.space.domain.repository.CurrentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class LocationResetScheduler {

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final CurrentLocationRepository
            currentLocationRepository;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    @Transactional
    public void resetAtMidnight() {
        currentLocationRepository.clearAllLocations();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void clearExpiredLocationsOnStartup() {
        LocalDate today =
                LocalDate.now(SERVICE_ZONE);

        currentLocationRepository
                .clearExpiredLocations(today);
    }
}