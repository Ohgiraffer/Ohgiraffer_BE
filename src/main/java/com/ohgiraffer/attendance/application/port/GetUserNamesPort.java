package com.ohgiraffer.attendance.application.port;

import java.util.List;
import java.util.Map;

public interface GetUserNamesPort {
    Map<Long, String> findNamesByUserIds(List<Long> userIds);
}
