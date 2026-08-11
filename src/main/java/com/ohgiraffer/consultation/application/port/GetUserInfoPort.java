package com.ohgiraffer.consultation.application.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GetUserInfoPort {

    String getUserName(Long userId);

    List<UserSummary> getUsersByRole(List<String> roles);

    Map<Long, String> getNames(List<Long> userIds);

    Optional<UserSummary> getUserSummary(Long userId);

    record UserSummary(Long userId, String name, String role) {}
}