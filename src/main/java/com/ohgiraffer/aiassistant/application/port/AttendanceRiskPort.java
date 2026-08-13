package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.aiassistant.domain.model.AttendanceRiskInfo;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface AttendanceRiskPort {

    List<AttendanceRiskInfo> getRiskItems(Long userId, Role role);

}
