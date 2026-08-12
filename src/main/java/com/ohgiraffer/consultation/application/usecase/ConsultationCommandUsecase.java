package com.ohgiraffer.consultation.application.usecase;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;
import com.ohgiraffer.consultation.domain.model.SaveRecordResult;

public interface ConsultationCommandUsecase {

    Long requestConsultation(RequestConsultationCommand command);

    SaveRecordResult saveRecord(SaveRecordCommand command);

    void registerAvailableTime(RegisterAvailableTimeCommand command);
}