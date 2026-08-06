package com.ohgiraffer.bootcamp.domain.model;

import com.ohgiraffer.bootcamp.application.policy.AttendancePeriodPolicy;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Bootcamp {

    private final Long id;
    private String orgName;
    private String proName;
    private LocalDate startDate;
    private LocalDate endDate;

    private Bootcamp(Long id, String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.orgName = orgName;
        this.proName = proName;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    // 신규 생성
    public static Bootcamp create(String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        return new Bootcamp(null, orgName, proName, startDate, endDate);
    }

    // 저장된 데이터로부터 복원
    public static Bootcamp reconstruct(Long id, String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        return new Bootcamp(id, orgName, proName, startDate, endDate);
    }

    // 수정
    public void changeInfo(String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        LocalDate newStart = startDate != null ? startDate : this.startDate;
        LocalDate newEnd = endDate != null ? endDate : this.endDate;

        if (newStart.isAfter(newEnd)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_RANGE);
        }

        if (orgName != null) this.orgName = orgName;
        if (proName != null) this.proName = proName;
        this.startDate = newStart;
        this.endDate = newEnd;
    }
}