package com.ohgiraffer.attendance.application.port;

import java.util.List;

public interface GetBootcampStaffIdsPort {

    // 강사+매니저 전체 조회 (출결 위험 알림은 강사도 대상이라 매니저만 조회하는 전자결재와 다름)
    List<Long> findStaffIdsByBootcampId(Long bootcampId);

}
