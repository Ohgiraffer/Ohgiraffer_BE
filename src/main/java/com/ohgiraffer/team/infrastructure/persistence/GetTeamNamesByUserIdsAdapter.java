package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.user.application.port.GetTeamNamesByUserIdsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetTeamNamesByUserIdsAdapter implements GetTeamNamesByUserIdsPort {

    private final SpringDataTeamMemberRepository springDataTeamMemberRepository;

    @Override
    public Map<Long, String> findTeamNamesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return springDataTeamMemberRepository.findActiveTeamNamesByUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        UserTeamNameProjection::getUserId,
                        UserTeamNameProjection::getTeamName,
                        (existing, replacement) -> existing
                ));
    }
}