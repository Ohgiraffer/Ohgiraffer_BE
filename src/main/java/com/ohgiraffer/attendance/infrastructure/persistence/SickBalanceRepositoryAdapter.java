package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SickBalanceRepositoryAdapter implements SickBalanceRepository {

    private final SpringDataSickBalanceRepository springDataSickBalanceRepository;

    @Override
    public Optional<SickBalance> findCurrentByUserId(Long userId, LocalDate referenceDate) {
        return springDataSickBalanceRepository
                .findByUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId, referenceDate, referenceDate)
                .map(this::toDomain);
    }

    @Override
    public Optional<SickBalance> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd) {
        return springDataSickBalanceRepository
                .findByUserIdAndPeriodEnd(userId, periodEnd)
                .map(this::toDomain);
    }

    @Override
    public Optional<SickBalance> findByUserIdAndPeriodStart(Long userId, LocalDate periodStart) {
        return springDataSickBalanceRepository
                .findByUserIdAndPeriodStart(userId, periodStart)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart) {
        return springDataSickBalanceRepository.existsByUserIdAndPeriodStart(userId, periodStart);
    }

    @Override
    public SickBalance save(SickBalance sickBalance) {
        SickBalanceJpaEntity entity = SickBalanceJpaEntity.of(
                sickBalance.getUserId(),
                sickBalance.getPeriodStart(),
                sickBalance.getPeriodEnd(),
                sickBalance.getTotalDays(),
                sickBalance.getUsedDays(),
                sickBalance.getCarriedOverDays()
        );
        return toDomain(springDataSickBalanceRepository.save(entity));
    }

    private SickBalance toDomain(SickBalanceJpaEntity entity) {
        return SickBalance.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getTotalDays(),
                entity.getUsedDays(),
                entity.getCarriedOverDays()
        );
    }
}