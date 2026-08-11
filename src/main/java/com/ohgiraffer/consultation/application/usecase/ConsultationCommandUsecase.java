package com.ohgiraffer.consultation.application.usecase;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;

public interface ConsultationCommandUsecase {

    Long requestConsultation(RequestConsultationCommand command);

    void saveRecord(SaveRecordCommand command);

    void registerAvailableTime(RegisterAvailableTimeCommand command);
}