package com.ohgiraffer.evaluation.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평가 동기화 알림 발송 결과")
public record EvaluationSyncNotifyResponse(

        @Schema(
                description = """
                        알림을 보낸 사람 수. 보낸 사람 자신은 빼고 센다.
                        """,
                example = "4"
        )
        int notifiedCount
) {
}
