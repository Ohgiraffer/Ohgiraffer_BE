package com.ohgiraffer.consultation.infrastructure.scheduler;
import com.ohgiraffer.consultation.domain.model.Consultation;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultationExpireScheduler {

    private final ConsultationRepository consultationRepository;

    @Scheduled(cron = "0 0 * * * *") // 매 정시
    @Transactional
    public void expirePastConsultations() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(1);

        List<Consultation> targets = consultationRepository
                .findByStatusAndScheduledAtBefore(ConsultationStatus.PENDING, deadline);

        targets.forEach(c -> {
            c.expire();
            consultationRepository.save(c);
        });

        log.info("[상담 자동취소] {}건 처리", targets.size());
    }
}