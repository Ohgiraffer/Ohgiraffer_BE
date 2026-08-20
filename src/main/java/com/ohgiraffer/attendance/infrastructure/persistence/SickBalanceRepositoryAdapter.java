package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.SickBalance;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SickBalanceRepositoryAdapter implements SickBalanceRepository {

    private final SpringDataSickBalanceRepository springDataSickBalanceRepository;

    @Override
    public Optional<SickBalance> findByUserId(Long userId) {
        return springDataSickBalanceRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public SickBalance save(SickBalance sickBalance) {
        SickBalanceJpaEntity entity = sickBalance.getId() != null
                ? SickBalanceJpaEntity.reconstitute(sickBalance.getId(), sickBalance.getUserId(), sickBalance.getTotalDays(), sickBalance.getUsedDays())
                : SickBalanceJpaEntity.of(sickBalance.getUserId(), sickBalance.getTotalDays(), sickBalance.getUsedDays());
        return toDomain(springDataSickBalanceRepository.save(entity));
    }

    @Override
    public boolean tryConsume(Long userId, BigDecimal amount) {
        return springDataSickBalanceRepository.tryConsume(userId, amount) > 0;
    }

    private SickBalance toDomain(SickBalanceJpaEntity entity) {
        return SickBalance.reconstitute(entity.getId(), entity.getUserId(), entity.getTotalDays(), entity.getUsedDays());
    }
}