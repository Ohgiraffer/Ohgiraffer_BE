package com.ohgiraffer.auditlog.application.usecase;

import com.ohgiraffer.auditlog.application.command.RecordAuditLogCommand;

public interface RecordAuditLogUsecase {

    void record(RecordAuditLogCommand command);
}