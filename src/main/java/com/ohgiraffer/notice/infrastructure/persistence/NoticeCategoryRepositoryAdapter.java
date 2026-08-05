package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoticeCategoryRepositoryAdapter implements NoticeCategoryRepository {

    private static final Sort BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final SpringDataNoticeCategoryRepository springDataNoticeCategoryRepository;

    public NoticeCategoryRepositoryAdapter(
            SpringDataNoticeCategoryRepository springDataNoticeCategoryRepository
    ) {
        this.springDataNoticeCategoryRepository = springDataNoticeCategoryRepository;
    }

    @Override
    public boolean existsById(Long categoryId) {
        return springDataNoticeCategoryRepository.existsById(categoryId);
    }

    @Override
    public Optional<NoticeCategory> findById(Long categoryId) {
        return springDataNoticeCategoryRepository.findById(categoryId)
                .map(NoticeCategoryJpaEntity::toDomain);
    }

    @Override
    public List<NoticeCategory> findAll() {
        return springDataNoticeCategoryRepository.findAll(BY_ID)
                .stream()
                .map(NoticeCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public NoticeCategory save(NoticeCategory category) {
        return springDataNoticeCategoryRepository
                .save(NoticeCategoryJpaEntity.from(category))
                .toDomain();
    }

    @Override
    public void deleteById(Long categoryId) {
        springDataNoticeCategoryRepository.deleteById(categoryId);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataNoticeCategoryRepository.existsByName(name);
    }
}
