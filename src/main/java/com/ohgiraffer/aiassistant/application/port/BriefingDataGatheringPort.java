package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import com.ohgiraffer.user.domain.model.Role;

public interface BriefingDataGatheringPort {

    // 브리핑 생성에 필요한 원본 데이터 전부 취합
    BriefingSourceData gather(Long userId, Role role);

}
