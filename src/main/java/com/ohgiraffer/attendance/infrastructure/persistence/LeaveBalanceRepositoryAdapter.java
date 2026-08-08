package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeaveBalanceRepositoryAdapter implements LeaveBalanceRepository {

    private final SpringDataLeaveBalanceRepository springDataLeaveBalanceRepository;

    @Override
    public Optional<LeaveBalance> findCurrentByUserId(Long userId, LocalDate referenceDate) {
        return springDataLeaveBalanceRepository
                .findByUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(userId, referenceDate, referenceDate)
                .map(this::toDomain);
    }

    @Override
    public Optional<LeaveBalance> findByUserIdAndPeriodEnd(Long userId, LocalDate periodEnd) {
        return springDataLeaveBalanceRepository
                .findByUserIdAndPeriodEnd(userId, periodEnd)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart) {
        return springDataLeaveBalanceRepository.existsByUserIdAndPeriodStart(userId, periodStart);
    }

    @Override
    public LeaveBalance save(LeaveBalance leaveBalance) {
        LeaveBalanceJpaEntity entity = LeaveBalanceJpaEntity.of(
                leaveBalance.getUserId(),
                leaveBalance.getPeriodStart(),
                leaveBalance.getPeriodEnd(),
                leaveBalance.getTotalDays(),
                leaveBalance.getUsedDays(),
                leaveBalance.getCarriedOverDays()
        );
        return toDomain(springDataLeaveBalanceRepository.save(entity));
    }

    private LeaveBalance toDomain(LeaveBalanceJpaEntity entity) {
        return LeaveBalance.reconstitute(
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