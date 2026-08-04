package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.notice.application.usecase.NoticeCategoryQueryUseCase;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoticeCategoryQueryService implements NoticeCategoryQueryUseCase {

    private final NoticeCategoryRepository noticeCategoryRepository;

    public NoticeCategoryQueryService(
            NoticeCategoryRepository noticeCategoryRepository
    ) {
        this.noticeCategoryRepository = noticeCategoryRepository;
    }

    @Override
    public List<NoticeCategory> findAll() {
        return noticeCategoryRepository.findAll();
    }
}
