package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.LeaveBalance;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeaveBalanceRepositoryAdapter implements LeaveBalanceRepository {

    private final SpringDataLeaveBalanceRepository springDataLeaveBalanceRepository;

    @Override
    public Optional<LeaveBalance> findByUserId(Long userId) {
        return springDataLeaveBalanceRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public LeaveBalance save(LeaveBalance leaveBalance) {
        LeaveBalanceJpaEntity entity = leaveBalance.getId() != null
                ? LeaveBalanceJpaEntity.reconstitute(
                leaveBalance.getId(), leaveBalance.getUserId(), leaveBalance.getTotalDays(), leaveBalance.getUsedDays())
                : LeaveBalanceJpaEntity.of(
                leaveBalance.getUserId(), leaveBalance.getTotalDays(), leaveBalance.getUsedDays());

        return toDomain(springDataLeaveBalanceRepository.save(entity));
    }

    @Override
    public boolean tryConsume(Long userId, BigDecimal amount) {
        return springDataLeaveBalanceRepository.tryConsume(userId, amount) > 0;
    }

    private LeaveBalance toDomain(LeaveBalanceJpaEntity entity) {
        return LeaveBalance.reconstitute(entity.getId(), entity.getUserId(), entity.getTotalDays(), entity.getUsedDays());
    }
}