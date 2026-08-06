package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            /*
             * saveAndFlush 로 INSERT 를 지금 내보낸다.
             * save 만 하면 트랜잭션이 끝날 때 나가 이 catch 를 지나쳐 버린다.
             */
            return springDataNoticeCategoryRepository
                    .saveAndFlush(NoticeCategoryJpaEntity.from(category))
                    .toDomain();
        } catch (DataIntegrityViolationException e) {
            /*
             * 서비스가 미리 이름 중복을 검사하지만, 검사와 저장 사이에 같은 이름이 들어오면
             * DB 의 UNIQUE 제약이 막는다. 이때도 500 이 아니라 중복과 같은 409 로 알린다.
             */
            throw ConstraintViolations.translate(
                    e,
                    ConstraintViolations.UNIQUE_CATEGORY_NAME,
                    ErrorCode.NOTICE_CATEGORY_DUPLICATE_NAME,
                    null
            );
        }
    }

    @Override
    public void deleteById(Long categoryId) {
        try {
            springDataNoticeCategoryRepository.deleteById(categoryId);

            /*
             * DELETE 도 트랜잭션이 끝날 때 나가므로 여기서 밀어내야 catch 가 동작한다.
             */
            springDataNoticeCategoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            /*
             * 서비스가 사용 중인지 미리 세어 보지만, 세고 나서 지우기 전에 그 카테고리로
             * 공지가 등록되면 외래키가 막는다. 이때도 사용 중과 같은 409 로 알린다.
             * 다만 몇 건인지는 알 수 없어 건수 없는 기본 문구로 나간다.
             */
            throw ConstraintViolations.translate(
                    e,
                    ConstraintViolations.NOTICE_TO_CATEGORY,
                    ErrorCode.NOTICE_CATEGORY_IN_USE,
                    "공지가 사용 중이라 삭제할 수 없습니다."
                            + " 목록을 새로고침한 뒤 다시 확인해주세요."
            );
        }
    }

    @Override
    public boolean existsByName(String name) {
        return springDataNoticeCategoryRepository.existsByName(name);
    }
}
