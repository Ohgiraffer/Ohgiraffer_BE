package com.ohgiraffer.bootcamp.application.policy;

import com.ohgiraffer.bootcamp.application.command.PeriodCommand;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;
import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendancePeriodChangeLogPolicy {
    private AttendancePeriodChangeLogPolicy() {}

    public static List<SettingChangeLog> diff(Long bootcampId, Long userId,
                                              List<AttendancePeriod> oldPeriods,
                                              List<PeriodCommand> newPeriods) {
        List<SettingChangeLog> logs = new ArrayList<>();

        Map<Integer, AttendancePeriod> oldByNo = oldPeriods.stream()
                .collect(Collectors.toMap(AttendancePeriod::getPeriodNo, p -> p));

        for (PeriodCommand p : newPeriods) {
            AttendancePeriod old = oldByNo.get(p.periodNo());
            String startField = p.periodNo() + "단위기간 시작일";
            String endField = p.periodNo() + "단위기간 종료일";

            if (old == null) {
                logs.addAll(SettingChangePolicy.diff(bootcampId, userId, startField, null, p.periodStart()));
                logs.addAll(SettingChangePolicy.diff(bootcampId, userId, endField, null, p.periodEnd()));
            } else {
                logs.addAll(SettingChangePolicy.diff(bootcampId, userId, startField, old.getPeriodStart(), p.periodStart()));
                logs.addAll(SettingChangePolicy.diff(bootcampId, userId, endField, old.getPeriodEnd(), p.periodEnd()));
            }
        }

        for (AttendancePeriod old : oldPeriods) {
            boolean stillExists = newPeriods.stream().anyMatch(p -> p.periodNo().equals(old.getPeriodNo()));
            if (!stillExists) {
                logs.add(SettingChangeLog.create(
                        bootcampId, userId, old.getPeriodNo() + "단위기간 삭제",
                        old.getPeriodStart() + " ~ " + old.getPeriodEnd(), null));
            }
        }

        return logs;
    }
}