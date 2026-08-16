package com.ohgiraffer.ai.domain.dto;

import com.ohgiraffer.ai.domain.model.FailReason;

public record FailReasonCount(
        FailReason failReason, long count
) {
}