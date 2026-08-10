package com.ohgiraffer.space.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.space.application.command.UpdateMyLocationCommand;
import com.ohgiraffer.space.application.usecase.MyLocationResult;
import com.ohgiraffer.space.application.usecase.UpdateMyLocationUseCase;
import com.ohgiraffer.space.domain.model.Space;
import com.ohgiraffer.space.domain.repository.CurrentLocationRepository;
import com.ohgiraffer.space.domain.repository.SpaceRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateMyLocationService
        implements UpdateMyLocationUseCase {

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final SpaceRepository spaceRepository;
    private final CurrentLocationRepository currentLocationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MyLocationResult update(
            UpdateMyLocationCommand command,
            Long requesterId,
            Role requesterRole
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        User user = userRepository.findById(requesterId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "활성 사용자만 위치를 변경할 수 있습니다."
            );
        }

        Long requestedSpaceId =
                command.spaceId();

        if (requestedSpaceId == null) {
            currentLocationRepository.clearLocation(
                    requesterId
            );

            return MyLocationResult.cleared(user);
        }

        validateSpaceId(requestedSpaceId);

        LocalDate today =
                LocalDate.now(SERVICE_ZONE);

        Space targetSpace =
                spaceRepository
                        .findByIdForUpdate(
                                requestedSpaceId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SPACE_NOT_FOUND
                                )
                        );

        Optional<Long> currentSpaceId =
                currentLocationRepository
                        .findCurrentSpaceId(
                                requesterId,
                                today
                        );

        if (currentSpaceId.isPresent()
                && currentSpaceId.get()
                .equals(requestedSpaceId)) {
            currentLocationRepository.saveLocation(
                    requesterId,
                    requestedSpaceId,
                    today
            );

            return MyLocationResult.located(
                    user,
                    targetSpace
            );
        }

        long currentCount =
                spaceRepository.countOccupants(
                        requestedSpaceId,
                        today
                );

        if (currentCount >= targetSpace.getCapacity()) {
            throw new BusinessException(
                    ErrorCode.SPACE_CAPACITY_EXCEEDED
            );
        }

        currentLocationRepository.saveLocation(
                requesterId,
                requestedSpaceId,
                today
        );

        return MyLocationResult.located(
                user,
                targetSpace
        );
    }

    private void validateRequester(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        if (requesterRole != Role.STUDENT
                && requesterRole != Role.INSTRUCTOR
                && requesterRole != Role.MANAGER) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private void validateSpaceId(
            Long spaceId
    ) {
        if (spaceId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공간 ID가 올바르지 않습니다."
            );
        }
    }
}