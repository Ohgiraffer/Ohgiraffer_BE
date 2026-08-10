package com.ohgiraffer.space.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.space.application.usecase.DeleteSpaceUseCase;
import com.ohgiraffer.space.domain.repository.SpaceRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DeleteSpaceService
        implements DeleteSpaceUseCase {

    private final SpaceRepository spaceRepository;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public void delete(
            Long spaceId,
            Long requesterId,
            Role requesterRole
    ) {
        validateSpaceId(spaceId);

        validateManagementAuthority(
                requesterId,
                requesterRole
        );

        spaceRepository.findByIdForUpdate(spaceId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SPACE_NOT_FOUND
                        )
                );

        LocalDate today =
                LocalDate.now(SERVICE_ZONE);

        if (spaceRepository.hasOccupants(
                spaceId,
                today
        )) {
            throw new BusinessException(
                    ErrorCode.SPACE_HAS_OCCUPANTS
            );
        }

        spaceRepository.deleteById(spaceId);
    }

    private void validateSpaceId(
            Long spaceId
    ) {
        if (spaceId == null || spaceId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공간 ID가 올바르지 않습니다."
            );
        }
    }

    private void validateManagementAuthority(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SPACE_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.SPACE_ACCESS_DENIED
            );
        }
    }
}