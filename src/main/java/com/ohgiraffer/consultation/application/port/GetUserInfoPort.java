package com.ohgiraffer.consultation.application.port;

import com.ohgiraffer.consultation.domain.model.UserSummary;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GetUserInfoPort {

    String getUserName(Long userId);

    List<UserSummary> getUsersByRole(List<String> roles);

    Map<Long, String> getNames(List<Long> userIds);

    Optional<UserSummary> getUserSummary(Long userId);

    boolean existsById(Long userId);
}