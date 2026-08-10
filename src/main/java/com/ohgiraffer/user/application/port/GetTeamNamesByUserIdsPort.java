package com.ohgiraffer.user.application.port;

import java.util.List;
import java.util.Map;

public interface GetTeamNamesByUserIdsPort {
    Map<Long, String> findTeamNamesByUserIds(List<Long> userIds);
}
