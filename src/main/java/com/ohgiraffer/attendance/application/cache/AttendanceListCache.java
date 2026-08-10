package com.ohgiraffer.attendance.application.cache;

import com.ohgiraffer.attendance.application.helper.StudentAttendanceRateResolver;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.attendance.domain.model.StudentAttendanceRateResult;
import com.ohgiraffer.attendance.presentation.api.response.StudentAttendanceSummaryResponse;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceListCache {

    private final UserQueryUsecase userQueryUsecase;
    private final GetUserNamesPort getUserNamesPort;
    private final StudentAttendanceRateResolver studentAttendanceRateResolver;

    @Cacheable(value = "attendanceList", key = "#bootcampId + '-' + T(java.time.LocalDate).now()")
    public List<StudentAttendanceSummaryResponse> getCachedSummaries(Long bootcampId) {
        List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(bootcampId);
        Map<Long, String> nameByUserId = getUserNamesPort.findNamesByUserIds(studentIds);
        Map<Long, StudentAttendanceRateResult> rateByUserId = studentAttendanceRateResolver.resolve(bootcampId, studentIds);

        return studentIds.stream()
                .map(userId -> {
                    String name = nameByUserId.getOrDefault(userId, "알 수 없음");
                    StudentAttendanceRateResult r = rateByUserId.get(userId);
                    return StudentAttendanceSummaryResponse.of(name, r.attendanceRate(), r.counts(), r.riskLevel());
                })
                .toList();
    }
}