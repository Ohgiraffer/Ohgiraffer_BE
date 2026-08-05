package com.ohgiraffer.bootcamp.application.service;

import com.ohgiraffer.bootcamp.application.usecase.BootcampCommandUsecase;
import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BootcampCommandService implements BootcampCommandUsecase {

    private final BootcampRepository bootcampRepository;

    @Override
    public Long register(String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        Bootcamp saved = bootcampRepository.save(Bootcamp.create(orgName, proName, startDate, endDate));

        log.info("[register] 부트캠프 등록 완료 | bootcampId={}, orgName={}", saved.getId(), saved.getOrgName());

        return saved.getId();
    }

    @Override
    public void update(Long bootcampId, String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        bootcamp.changeInfo(orgName, proName, startDate, endDate);
        bootcampRepository.save(bootcamp);

        log.info("[update] 부트캠프 수정 완료 | bootcampId={}, orgName={}", bootcampId, orgName);
    }
}