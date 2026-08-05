package com.ohgiraffer.bootcamp.application.policy;

import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BootcampInfoChangeLogPolicy{
    private BootcampInfoChangeLogPolicy() {}

    public static List<SettingChangeLog> diff(Long userId, Bootcamp before,
                                              String orgName, String proName,
                                              LocalDate startDate, LocalDate endDate) {
        List<SettingChangeLog> logs = new ArrayList<>();
        logs.addAll(SettingChangePolicy.diff(userId, "기관명", before.getOrgName(), orgName));
        logs.addAll(SettingChangePolicy.diff(userId, "과정명", before.getProName(), proName));
        logs.addAll(SettingChangePolicy.diff(userId, "부트캠프 시작일", before.getStartDate(), startDate));
        logs.addAll(SettingChangePolicy.diff(userId, "부트캠프 종료일", before.getEndDate(), endDate));
        return logs;
    }
}

