package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.SyncResult;

public class AttendanceSheetSyncResultResolver {

    private AttendanceSheetSyncResultResolver() {
    }

    public static SyncResult resolve(int totalCount, int failedCount) {
        if (failedCount == 0) {
            return SyncResult.SUCCESS;
        }
        if (totalCount > failedCount) {
            return SyncResult.PARTIAL; // 일부 성공
        }
        return SyncResult.FAIL;
    }
}