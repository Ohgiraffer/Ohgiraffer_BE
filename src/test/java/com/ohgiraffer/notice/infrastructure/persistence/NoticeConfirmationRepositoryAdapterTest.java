package com.ohgiraffer.notice.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 확인 기록을 남기는 방식이 경합에 견디는지 확인한다.
 *
 * <p>존재 여부를 묻고 저장하면 같은 사용자의 두 번째 요청이 그 사이에 끼어들어
 * 둘 다 저장을 시도하고 복합 PK 중복으로 터진다. 단일 INSERT 여야 한다.
 */
@ExtendWith(MockitoExtension.class)
class NoticeConfirmationRepositoryAdapterTest {

    private static final Long NOTICE_ID = 10L;
    private static final Long USER_ID = 7L;

    @Mock
    private SpringDataNoticeConfirmationRepository
            springDataNoticeConfirmationRepository;

    private NoticeConfirmationRepositoryAdapter
            noticeConfirmationRepositoryAdapter;

    @BeforeEach
    void setUp() {
        noticeConfirmationRepositoryAdapter =
                new NoticeConfirmationRepositoryAdapter(
                        springDataNoticeConfirmationRepository
                );
    }

    @Test
    @DisplayName("확인 처리는 존재 여부를 묻지 않고 한 문장으로 넣는다")
    void confirmInsertsWithoutCheckingFirst() {
        noticeConfirmationRepositoryAdapter.confirm(NOTICE_ID, USER_ID);

        verify(springDataNoticeConfirmationRepository)
                .insertIfAbsent(NOTICE_ID, USER_ID);

        /*
         * 미리 묻는 순간 그 사이에 두 번째 요청이 끼어들 틈이 생긴다.
         */
        verify(springDataNoticeConfirmationRepository, never())
                .existsById(any());
    }

    @Test
    @DisplayName("같은 확인을 두 번 요청해도 오류 없이 넘어간다")
    void confirmIsIdempotent() {
        noticeConfirmationRepositoryAdapter.confirm(NOTICE_ID, USER_ID);
        noticeConfirmationRepositoryAdapter.confirm(NOTICE_ID, USER_ID);

        verify(springDataNoticeConfirmationRepository, times(2))
                .insertIfAbsent(NOTICE_ID, USER_ID);
    }

    @Test
    @DisplayName("조회할 공지가 없으면 저장소를 부르지 않고 빈 결과를 돌려준다")
    void findConfirmedNoticeIdsSkipsEmptyInput() {
        Set<Long> confirmed = noticeConfirmationRepositoryAdapter
                .findConfirmedNoticeIds(USER_ID, List.of());

        assertTrue(confirmed.isEmpty());
        verify(springDataNoticeConfirmationRepository, never())
                .findConfirmedNoticeIds(any(), any());
    }

    @Test
    @DisplayName("확인한 공지 식별자를 집합으로 돌려준다")
    void findConfirmedNoticeIds() {
        when(springDataNoticeConfirmationRepository
                .findConfirmedNoticeIds(USER_ID, List.of(NOTICE_ID)))
                .thenReturn(List.of(NOTICE_ID));

        Set<Long> confirmed = noticeConfirmationRepositoryAdapter
                .findConfirmedNoticeIds(USER_ID, List.of(NOTICE_ID));

        assertEquals(Set.of(NOTICE_ID), confirmed);
    }
}
