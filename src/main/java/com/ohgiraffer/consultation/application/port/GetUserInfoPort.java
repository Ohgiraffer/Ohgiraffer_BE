package com.ohgiraffer.consultation.application.port;

import java.util.List;

public interface GetUserInfoPort {

    String getUserName(Long userId);

    List<UserSummary> getUsersByRole(List<String> roles);

    record UserSummary(Long userId, String name, String role) {}
}