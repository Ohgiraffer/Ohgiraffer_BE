package com.ohgiraffer.user.infrastructure.scheduler;

import com.ohgiraffer.attendance.application.usecase.AttendanceCacheEvictUsecase;
import com.ohgiraffer.user.application.port.GetBootcampIdsEndingOnPort;
import com.ohgiraffer.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCompletionScheduler {

    private final GetBootcampIdsEndingOnPort getBootcampIdsEndingOnPort;
    private final SpringDataUserRepository springDataUserRepository;
    private final AttendanceCacheEvictUsecase attendanceCacheEvictUsecase;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void completeStudentsForEndedBootcamps() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Long> endingBootcampIds = getBootcampIdsEndingOnPort.findBootcampIdsEndingOn(today);

        if (endingBootcampIds.isEmpty()) {
            log.info("[completeStudentsForEndedBootcamps] 오늘 종료되는 부트캠프 없음 | date={}", today);
            return;
        }

        for (Long bootcampId : endingBootcampIds) {
            int completedCount = springDataUserRepository.completeActiveStudentsByBootcampId(bootcampId);

            if (completedCount > 0) {
                attendanceCacheEvictUsecase.evictAllForBootcamp(bootcampId);
            }

            log.info("[completeStudentsForEndedBootcamps] 수료 처리 완료 | bootcampId={}, count={}",
                    bootcampId, completedCount);
        }
    }
}