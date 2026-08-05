package com.ohgiraffer.bootcamp.application.port;

import java.util.List;
import java.util.Map;

public interface GetUserNamesPort {
    Map<Long, String> findNamesByUserIds(List<Long> userIds);
}
