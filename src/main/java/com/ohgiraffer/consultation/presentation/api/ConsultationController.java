package com.ohgiraffer.consultation.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consultation")
@Tag(name="Consultation - 상담 정보 관리", description = "상담 정보와 설정을 다루기 위한 컨트롤러")
public class ConsultationController {
}
