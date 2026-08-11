package com.ohgiraffer.consultation.domain.model;

import com.ohgiraffer.user.domain.model.Role;

public record CounselorInfo(
        Long counselorId, String name, Role role, String profileImgUrl
) {}