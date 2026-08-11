package com.ohgiraffer.consultation.application.usecase;

import com.ohgiraffer.consultation.domain.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface ConsultationQueryUsecase {

    List<CounselorInfo> getCounselors();

    Set<LocalDate> getAvailableDates(Long counselorId, YearMonth yearMonth);

    List<AvailableTimeSlot> getAvailableTimes(Long counselorId, LocalDate date);

    List<LocalTime> getRegisteredTimes(Long counselorId, LocalDate date);

    List<ConsultationSummary> getMyConsultations(Long userId);

    ConsultationDetail getDetail(Long consultationId, Long callerId, String callerRole);

    List<ConsultationListItem> getUpcoming(Long callerId);

    List<ConsultationListItem> getHistory();
}