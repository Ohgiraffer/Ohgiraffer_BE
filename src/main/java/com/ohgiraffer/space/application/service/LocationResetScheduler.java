package com.ohgiraffer.space.application.service;

import com.ohgiraffer.space.domain.repository.CurrentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LocationResetScheduler {


    private final CurrentLocationRepository currentLocationRepository;
    private final Clock clock;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    @Transactional
    public void resetAtMidnight() {
        LocalDate today =
                LocalDate.now(clock);

        currentLocationRepository
                .clearExpiredLocations(today);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void clearExpiredLocationsOnStartup() {
        LocalDate today =
                LocalDate.now(clock);

        currentLocationRepository
                .clearExpiredLocations(today);
    }
}